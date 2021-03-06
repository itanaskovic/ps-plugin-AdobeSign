/*
 *  Copyright 2016 Adobe Systems Incorporated. All rights reserved.
 *  This file is licensed to you under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License. You may obtain a copy
 *  of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
 *  OF ANY KIND, either express or implied. See the License for the specific language
 *  governing permissions and limitations under the License.
 *
 */
package com.appiancorp.ps.plugins.adobesign.api;

import java.util.ArrayList;
import java.util.List;

import com.adobe.sign.api.AgreementsApi;
import com.adobe.sign.model.agreements.AgreementCreationInfo;
import com.adobe.sign.model.agreements.AgreementCreationResponse;
import com.adobe.sign.model.agreements.AgreementDocuments;
import com.adobe.sign.model.agreements.DocumentCreationInfo;
import com.adobe.sign.model.agreements.FileInfo;
import com.adobe.sign.model.agreements.InteractiveOptions;
import com.adobe.sign.model.agreements.RecipientInfo;
import com.adobe.sign.model.agreements.RecipientSetInfo;
import com.adobe.sign.model.agreements.UserAgreement;
import com.adobe.sign.model.agreements.UserAgreements;
import com.adobe.sign.utils.ApiException;
import com.appiancorp.ps.plugins.adobesign.AdobeSignUtils;

public class AgreementUtils {

	public final static AgreementsApi agreementsApi = new AgreementsApi();

	// Document identifier to identify whether document is library document or
	// transient document
	public enum DocumentIdentifierType {
		TRANSIENT_DOCUMENT_ID, LIBRARY_DOCUMENT_ID, LIBRARY_DOCUMENT_NAME;
	}

	/**
	 * Creates an agreement.
	 *
	 * @param recipientSetEmailList
	 *            List containing email ids about the recipients.
	 * @param documentId
	 *            Id of the document
	 * @param documentIdentifierType
	 *            type of document
	 * @param agreementName
	 *            name of the agreement user wants to create
	 * @return AgreementCreationResponse object containing information of the
	 *         agreement.
	 */
	public static AgreementCreationResponse createAgreement(List<String> recipientSetEmailList, String documentId,
			DocumentIdentifierType documentIdentifierType, String agreementName) throws ApiException {
		try {
			return createAgreementWithRecipientSetName(recipientSetEmailList, null, documentId, documentIdentifierType, agreementName);
		} catch (ApiException e) {
			AdobeSignUtils.logException(Errors.CREATE_AGREEMENT, e);
			return null;
		}
	}

	/**
	 * Returns list containing information about the recipients.
	 *
	 * @param userEmails
	 *            List containing email ids about the recipients.
	 * @return RecipientSetInfo object containing information of the recipients.
	 */
	protected static List<RecipientSetInfo> getRecipientSetInfo(List<String> userEmails) {
		return getRecipientSetInfoWithRecipientSetName(userEmails, null);
	}

	/**
	 * Returns list containing information about the recipients.
	 *
	 * @param userEmails
	 *            List containing email ids about the recipients.
	 * @param recipientSetName
	 *            The name of the recipient set.
	 * @return RecipientSetInfo object containing information of the recipients.
	 */
	public static List<RecipientSetInfo> getRecipientSetInfoWithRecipientSetName(List<String> userEmails, String recipientSetName) {

		// Create an array of recipients from list of email ids.
		List<RecipientInfo> recipientSetMemberInfos = new ArrayList<RecipientInfo>();
		for (String userEmail : userEmails) {
			RecipientInfo recipientInfo = new RecipientInfo();
			recipientInfo.setEmail(userEmail);
			recipientSetMemberInfos.add(recipientInfo);
		}

		// Create recipient set info
		RecipientSetInfo recipientSetInfo = new RecipientSetInfo();
		recipientSetInfo.setRecipientSetMemberInfos(recipientSetMemberInfos);
		recipientSetInfo.setRecipientSetRole(RecipientSetInfo.RecipientSetRoleEnum.SIGNER);

		if (recipientSetName != null) recipientSetInfo.setRecipientSetName(recipientSetName);

		List<RecipientSetInfo> recipientSetInfos = new ArrayList<RecipientSetInfo>();
		recipientSetInfos.add(recipientSetInfo);
		return recipientSetInfos;
	}

	/**
	 * Get File info using document.
	 *
	 * @param documentId
	 *            id of document
	 * @param documentIdentifierType
	 *            type of document
	 * @return FileInfo object containing information about the file.
	 */
	public static FileInfo getFileInfo(String documentId, DocumentIdentifierType documentIdentifierType) {
		// Create file info object using document id and name
		FileInfo fileInfo = new FileInfo();
		if (documentIdentifierType.equals(DocumentIdentifierType.LIBRARY_DOCUMENT_ID)) {
			fileInfo.setLibraryDocumentId(documentId);
		} else if (documentIdentifierType.equals(DocumentIdentifierType.TRANSIENT_DOCUMENT_ID)) {
			fileInfo.setTransientDocumentId(documentId);
		} else if (documentIdentifierType.equals(DocumentIdentifierType.LIBRARY_DOCUMENT_NAME)) {
			fileInfo.setLibraryDocumentName(documentId);
		}
		return fileInfo;
	}

	/**
	 * Get document creation info.
	 *
	 * @param agreementName
	 *            Name of the agreement
	 * @param fileInfos
	 *            List of file info objects
	 * @param recipientSetInfos
	 *            List containing information about the recipients.
	 * @return DocumentCreationInfo object containing information required to
	 *         create a document.
	 */
	public static DocumentCreationInfo getDocumentCreationInfo(String agreementName, List<FileInfo> fileInfos,
			List<RecipientSetInfo> recipientSetInfos) {

		// Create document creation info from the file info object
		DocumentCreationInfo documentCreationInfo = new DocumentCreationInfo();
		documentCreationInfo.setName(agreementName);
		documentCreationInfo.setFileInfos(fileInfos);
		documentCreationInfo.setRecipientSetInfos(recipientSetInfos);
		documentCreationInfo.setSignatureType(DocumentCreationInfo.SignatureTypeEnum.ESIGN);
		documentCreationInfo.setSignatureFlow(DocumentCreationInfo.SignatureFlowEnum.SENDER_SIGNATURE_NOT_REQUIRED);
		return documentCreationInfo;
	}

	/**
	 * Get agreement creation info
	 *
	 * @param documentCreationInfo
	 *            information about the document to be used in the agreement
	 * @return AgreementCreationInfo object containing information required to
	 *         create an agreement.
	 */
	public static AgreementCreationInfo getAgreementCreationInfo(DocumentCreationInfo documentCreationInfo, InteractiveOptions interactiveOptions) {
		AgreementCreationInfo agreementCreationInfo = new AgreementCreationInfo();
		agreementCreationInfo.setDocumentCreationInfo(documentCreationInfo);
		agreementCreationInfo.setOptions(interactiveOptions);
		return agreementCreationInfo;
	}

	/**
	 * Return all the agreements of a user.
	 *
	 * @return UserAgreements object containing all the agreements of a user.
	 * @throws Exception
	 */
	public static UserAgreements getAllAgreements() throws ApiException {
		try {
			UserAgreements userAgreements = agreementsApi.getAgreements(AdobeSignUtils.getHeaderParams(), null, null, null, null);
			return userAgreements;
		} catch (ApiException e) {
			AdobeSignUtils.logException(Errors.GET_AGREEMENTS, e);
			return null;
		}
	}

	// /**
	// * Creates an alternate participant for the agreement.
	// *
	// * @param agreementId
	// * Id of the agreement.
	// * @return AlternateParticipantResponse object containing information
	// about
	// * the alternate participant.
	// * @throws Exception
	// */
	// public static AlternateParticipantResponse
	// createAlternateParticipant(String agreementId, String userEmail, String
	// userMessage)
	// throws ApiException {
	// try {
	// // Create alternate participant info
	// AlternateParticipantInfo alternateParticipantInfo = new
	// AlternateParticipantInfo();
	//
	// // Set email id of an alternate participant
	// alternateParticipantInfo.setEmail(userEmail);
	//
	// // Set private message for an alternate participant
	// alternateParticipantInfo.setPrivateMessage(userMessage);
	//
	// // Get the participant set id of first participant set
	// AgreementInfo agreementInfo =
	// AgreementUtils.getAgreementInfo(agreementId);
	// ParticipantSetInfo participantSetInfo =
	// agreementInfo.getParticipantSetInfos().get(0);
	// String participantSetId = participantSetInfo.getParticipantSetId();
	//
	// // Get participant id of first participant in the set
	// ParticipantInfo participantInfo =
	// participantSetInfo.getParticipantSetMemberInfos().get(0);
	// String participantId = participantInfo.getParticipantId();
	// AlternateParticipantResponse alternateParticipantResponse =
	// agreementsApi.createAlternateParticipant(headers, agreementId,
	// participantSetId, participantId, alternateParticipantInfo);
	// return alternateParticipantResponse;
	// } catch (ApiException e) {
	// AdobeSignUtils.logException(Errors.CREATE_ALTERNATE_PARTICIPANT_FOR_AGREEMENT,
	// e);
	// return null;
	// }
	// }

	// /**
	// * Retrieves the information of a given agreement.
	// *
	// * @param agreementId
	// * The agreement identifier.
	// * @return AgreementInfo
	// */
	// public static AgreementInfo getAgreementInfo(String agreementId) throws
	// ApiException {
	// try {
	// AgreementInfo agreementInfo = agreementsApi.getAgreementInfo(headers,
	// agreementId);
	// return agreementInfo;
	// } catch (ApiException e) {
	// AdobeSignUtils.logException(Errors.GET_AGREEMENT_DETAILS, e);
	// return null;
	// }
	// }

	// /**
	// * Check if next participant set is present and then print information
	// about
	// * it.
	// *
	// * @param agreementId
	// * agreement identifier
	// * @throws Exception
	// */
	// public static void printNextParticipantSetInfo(String agreementId) throws
	// ApiException {
	// // Make API call to get agreement information.
	// AgreementInfo agreementInfo = getAgreementInfo(agreementId);
	//
	// // Retrieve nextParticipantSetList information from JSON response.
	// List<NextParticipantSetInfo> nextParticipantSetInfoList =
	// agreementInfo.getNextParticipantSetInfos();
	//
	// // Check nextParticipantSetInfoList is not empty
	// if (nextParticipantSetInfoList == null ||
	// nextParticipantSetInfoList.isEmpty()) {
	// AdobeSignUtils.logError(Errors.NO_PARTICIPANT_SET);
	// AdobeSignUtils.getLogger().info("");
	// return;
	// }
	//
	// // Find next participant set and print its members
	// for (NextParticipantSetInfo nextParticipantSetInfo :
	// nextParticipantSetInfoList) {
	// if (nextParticipantSetInfo != null) {
	// printEachMemberOfParticipantSet(nextParticipantSetInfo);
	// }
	// }
	// }

	/**
	 * // * Display information (name, email and waiting time) about the next
	 * set of // * participants in the signing process of the specified
	 * agreement. // * // * @param nextParticipantSetInfo // * object containing
	 * information about the next participant set //
	 */
	// private static void
	// printEachMemberOfParticipantSet(NextParticipantSetInfo
	// nextParticipantSetInfo) {
	// // Find next participant set member
	// List<NextParticipantInfo> nextParticipantSetMemberInfoList =
	// nextParticipantSetInfo.getNextParticipantSetMemberInfos();
	// for (NextParticipantInfo nextParticipantInfo :
	// nextParticipantSetMemberInfoList) {
	//
	// // Display details about next participant set
	// AdobeSignUtils.getLogger().info("Information about next participant");
	// AdobeSignUtils.getLogger().info("Name = " + nextParticipantInfo.getName());
	// AdobeSignUtils.getLogger().info("Email = " + nextParticipantInfo.getEmail());
	// AdobeSignUtils.getLogger().info("Waiting Since = " +
	// nextParticipantInfo.getWaitingSince());
	// AdobeSignUtils.getLogger().info("");
	// }
	// }
	//
	// /**
	// * Return audit trail of the given agreement.
	// *
	// * @param agreementId
	// * Id of the agreement.
	// * @return Audit Trail
	// * @throws Exception
	// */
	// public static byte[] getAuditTrail(String agreementId) throws
	// ApiException {
	// try {
	// byte[] auditTrail = agreementsApi.getAuditTrail(headers, agreementId);
	//
	// return auditTrail;
	// } catch (ApiException e) {
	// AdobeSignUtils.logException(Errors.GET_AUDIT_TRAIL, e);
	// return null;
	// }
	// }
	//
	// /**
	// * Return a file stream of combined PDF document.
	// *
	// * @param agreementId
	// * Id of the agreement.
	// * @return array of bytes of documents of the given agreement.
	// * @throws Exception
	// */
	// public static byte[] getAgreementCombinedDocument(String agreementId)
	// throws ApiException {
	// try {
	// byte[] responseBytes = agreementsApi.getCombinedDocument(headers,
	// agreementId, null, null, true, false);
	//
	// return responseBytes;
	// } catch (ApiException e) {
	// AdobeSignUtils.logException(Errors.GET_COMBINED_DOCUMENT, e);
	// return null;
	// }
	// }
	//
	/**
	 * Retrieves the IDs of all the main and supporting documents of an
	 * agreement identified by agreement id.
	 *
	 * @param agreementId
	 *            The agreement identifier.
	 * @return AgreementDocuments
	 */
	public static AgreementDocuments getAllDocuments(String agreementId) throws ApiException {
		try {
			// Fetch list of documents associated with the specified agreement.
			AgreementDocuments agreementDocuments = agreementsApi.getAllDocuments(AdobeSignUtils.getHeaderParams(), agreementId, null, null, null);
			return agreementDocuments;
		} catch (ApiException e) {
			AdobeSignUtils.logException(Errors.GET_AGREEMENTS_DOCUMENTS, e);
			return null;
		}
	}

	/**
	 * Saves the contents of all the document associated with a specified
	 * agreement in files.<br>
	 * Note: The format of content can either be PDF or original, depending upon
	 * the format associated with ID.
	 *
	 * @param agreementId
	 *            The agreement ID.
	 * @param documentId
	 *            The document ID
	 * @return
	 * @throws Exception
	 */
	public static byte[] downloadDocuments(String agreementId, String documentId) throws ApiException {
		try {
			// Download and save the documents.
			byte docStream[] = agreementsApi.getDocument(AdobeSignUtils.getHeaderParams(), agreementId, documentId);
			return docStream;
		} catch (ApiException e) {
			AdobeSignUtils.logException(Errors.SAVE_DOCUMENTS, e);
			return null;
		}
	}

	/**
	 * Returns id of the agreement with given agreement name.
	 *
	 * @param agreementName
	 *            name of the agreement.
	 * @return String containing agreement id.
	 */
	public static String isExistingAgreement(String agreementName) throws ApiException {
		try {
			// Make API call to get all the agreements of a user.
			UserAgreements userAgreements = getAllAgreements();

			// Find first agreement which is out for signature and return its
			// agreement id.
			List<UserAgreement> userAgreementList = userAgreements.getUserAgreementList();
			for (UserAgreement userAgreement : userAgreementList) {
				if (userAgreement.getName().equals(agreementName)) {
					return userAgreement.getAgreementId();
				}
			}
			return null;
		} catch (ApiException e) {
			AdobeSignUtils.logException(Errors.CHECK_AGREEMENT_EXIST, e);
			return null;
		}
	}

	/**
	 * Returns id of the agreement with given agreement name if exists else it
	 * returns null
	 *
	 * @param agreementName
	 *            name of the agreement.
	 * @return String containing agreement id.
	 */
	public static String getAgreementId(String agreementName) throws ApiException {
		try {
			// Check if agreement exist and return agreement id.
			String agreementId = isExistingAgreement(agreementName);
			if (agreementId != null) {
				return agreementId;
			}
			return null;
		} catch (ApiException e) {
			AdobeSignUtils.logException(Errors.GET_AGREEMENT_ID, e);
			return null;
		}
	}

	//
	// /**
	// * Returns url of the agreement identified by agreement id.
	// *
	// * @param agreementId
	// * The agreement id.
	// * @return SigningUrlSetInfo containing list of signing urls.
	// */
	// public static List<SigningUrlSetInfo> getSigningUrl(String agreementId)
	// throws ApiException {
	// try {
	// // Fetch the signing url for the specified agreement
	// SigningUrlResponse signingUrlResponse =
	// agreementsApi.getSigningUrl(headers, agreementId);
	// // Returning the Arraylist of the url's
	// return signingUrlResponse.getSigningUrlSetInfos();
	// } catch (ApiException e) {
	// AdobeSignUtils.logException(Errors.GET_SIGNING_URL, e);
	// return null;
	// }
	// }
	//
	protected static AgreementCreationResponse createAgreementWithRecipientSetName(List<String> recipientSetEmailList, String recipientSetName,
			String documentId, DocumentIdentifierType documentIdentifierType, String agreementName) throws ApiException {
		try {
			// Get recipient set info
			List<RecipientSetInfo> recipientSetInfos = AgreementUtils
					.getRecipientSetInfoWithRecipientSetName(recipientSetEmailList, recipientSetName);

			// Get file info and create a list of file info
			FileInfo fileInfo = AgreementUtils.getFileInfo(documentId, documentIdentifierType);
			List<FileInfo> fileInfos = new ArrayList<FileInfo>();
			fileInfos.add(fileInfo);

			// Get document creation info using library document id
			DocumentCreationInfo documentCreationInfo = AgreementUtils.getDocumentCreationInfo(agreementName, fileInfos, recipientSetInfos);

			// Get agreement creation info
			AgreementCreationInfo agreementCreationInfo = AgreementUtils.getAgreementCreationInfo(documentCreationInfo, null);

			// Make API call to create agreement
			AgreementCreationResponse agreementCreationResponse = agreementsApi.createAgreement(AdobeSignUtils.getHeaderParams(), agreementCreationInfo);
			return agreementCreationResponse;
		} catch (ApiException e) {
			AdobeSignUtils.logException(Errors.CREATE_AGREEMENT, e);
			return null;
		}
	}

	public static AgreementCreationResponse createAgreement(AgreementCreationInfo agreementCreationInfo) throws ApiException {
		try {
			// Make API call to create agreement
			AgreementCreationResponse agreementCreationResponse = agreementsApi.createAgreement(AdobeSignUtils.getHeaderParams(), agreementCreationInfo);
			return agreementCreationResponse;
		} catch (ApiException e) {
			AdobeSignUtils.logException(Errors.CREATE_AGREEMENT, e);
			return null;
		}
	}
}