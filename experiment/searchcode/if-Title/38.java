/**
 * Copyright (c) 2014, The Board of Trustees of the University of Illinois
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.cdsframework.cda.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.cdsframework.util.LogUtils;
import org.openhealthtools.mdht.uml.cda.Act;
import org.openhealthtools.mdht.uml.cda.AssignedAuthor;
import org.openhealthtools.mdht.uml.cda.AssignedCustodian;
import org.openhealthtools.mdht.uml.cda.AssignedEntity;
import org.openhealthtools.mdht.uml.cda.Author;
import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.Component2;
import org.openhealthtools.mdht.uml.cda.Component3;
import org.openhealthtools.mdht.uml.cda.Component4;
import org.openhealthtools.mdht.uml.cda.Custodian;
import org.openhealthtools.mdht.uml.cda.CustodianOrganization;
import org.openhealthtools.mdht.uml.cda.DocumentationOf;
import org.openhealthtools.mdht.uml.cda.Entry;
import org.openhealthtools.mdht.uml.cda.EntryRelationship;
import org.openhealthtools.mdht.uml.cda.InfrastructureRootTypeId;
import org.openhealthtools.mdht.uml.cda.Section;
import org.openhealthtools.mdht.uml.cda.Informant12;
import org.openhealthtools.mdht.uml.cda.InformationRecipient;
import org.openhealthtools.mdht.uml.cda.IntendedRecipient;
import org.openhealthtools.mdht.uml.cda.Observation;
import org.openhealthtools.mdht.uml.cda.Organization;
import org.openhealthtools.mdht.uml.cda.Organizer;
import org.openhealthtools.mdht.uml.cda.ParentDocument;
import org.openhealthtools.mdht.uml.cda.Participant2;
import org.openhealthtools.mdht.uml.cda.ParticipantRole;
import org.openhealthtools.mdht.uml.cda.Patient;
import org.openhealthtools.mdht.uml.cda.PatientRole;
import org.openhealthtools.mdht.uml.cda.Person;
import org.openhealthtools.mdht.uml.cda.RecordTarget;
import org.openhealthtools.mdht.uml.cda.RelatedDocument;
import org.openhealthtools.mdht.uml.cda.ServiceEvent;
import org.openhealthtools.mdht.uml.hl7.datatypes.CD;
import org.openhealthtools.mdht.uml.hl7.datatypes.CE;
import org.openhealthtools.mdht.uml.hl7.datatypes.DatatypesFactory;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;
import org.openhealthtools.mdht.uml.hl7.datatypes.ON;
import org.openhealthtools.mdht.uml.hl7.datatypes.PN;
import org.openhealthtools.mdht.uml.hl7.datatypes.ST;
import org.openhealthtools.mdht.uml.hl7.datatypes.TS;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClass;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClassObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.ActClinicalDocument;
import org.openhealthtools.mdht.uml.hl7.vocab.ActMood;
import org.openhealthtools.mdht.uml.hl7.vocab.ActRelationshipHasComponent;
import org.openhealthtools.mdht.uml.hl7.vocab.ActRelationshipType;
import org.openhealthtools.mdht.uml.hl7.vocab.ContextControl;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityClass;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityClassOrganization;
import org.openhealthtools.mdht.uml.hl7.vocab.EntityDeterminer;
import org.openhealthtools.mdht.uml.hl7.vocab.ParticipationType;
import org.openhealthtools.mdht.uml.hl7.vocab.RoleClassRoot;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActClassDocumentEntryAct;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActClassDocumentEntryOrganizer;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActMoodDocumentObservation;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipDocument;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntry;
import org.openhealthtools.mdht.uml.hl7.vocab.x_ActRelationshipEntryRelationship;
import org.openhealthtools.mdht.uml.hl7.vocab.x_DocumentActMood;
import org.openhealthtools.mdht.uml.hl7.vocab.x_InformationRecipient;
import org.openhealthtools.mdht.uml.hl7.vocab.x_InformationRecipientRole;

/**
 *
 * @author HLN Consulting, LLC
 */
public class ClinicalDocumentUtils {

    final static public LogUtils logger = LogUtils.getLogger(ClinicalDocumentUtils.class);
    final static public String ILHIE_MentalHealth = "ILHIE_MentalHealth";
    final static public String ILHIE_HIV = "ILHIE_HIV";
    final static public String ILHIE_SubstanceAbuse = "ILHIE_SubstanceAbuse";

    /**
     * Initialize a new consent document.
     *
     * @return
     */
    public static ClinicalDocument createConsentDocument() {
        return createConsentDocument(new ArrayList<String>());
    }

    /**
     * Initialize a new consent document.
     *
     * @param disclosureClassificationList
     * @return
     */
    public static ClinicalDocument createConsentDocument(List<String> disclosureClassificationList) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

        ClinicalDocument clinicalDocument = CDAFactory.eINSTANCE.createClinicalDocument();
        clinicalDocument.setClassCode(ActClinicalDocument.DOCCLIN);
        clinicalDocument.setMoodCode(ActMood.EVN);

        // US realm code
        clinicalDocument.getRealmCodes().add(DatatypesFactory.eINSTANCE.createCS("US"));

        // Consent document type ID
        InfrastructureRootTypeId typeId = CDAFactory.eINSTANCE.createInfrastructureRootTypeId();
        typeId.setExtension("09230");
        clinicalDocument.setTypeId(typeId);

        // General Header Constraints
        II generalTemplateId = DatatypesFactory.eINSTANCE.createII("2.16.840.1.113883.10.20.3");
        clinicalDocument.getTemplateIds().add(generalTemplateId);

        // Consent Directive Header Constraints
        II ccdaTemplateId = DatatypesFactory.eINSTANCE.createII("2.16.840.1.113883.3.445.1");
        clinicalDocument.getTemplateIds().add(ccdaTemplateId);

        // assign random document ID
        II id = DatatypesFactory.eINSTANCE.createII("1.3.6.4.1.4.1.2835.888888", UUID.randomUUID().toString());
        clinicalDocument.setId(id);

        // Privacy Policy Acknowledgement Document
        CE code = DatatypesFactory.eINSTANCE.createCE("57016-8", "2.16.840.1.113883.6.1", "LOINC", "Privacy Policy Acknowledgement Document");
        clinicalDocument.setCode(code);

        // title
        ST title = DatatypesFactory.eINSTANCE.createST("Consent Authorization");
        clinicalDocument.setTitle(title);

        // effective time
        TS effectiveTime = DatatypesFactory.eINSTANCE.createIVL_TS(simpleDateFormat.format(new Date()));
        clinicalDocument.setEffectiveTime(effectiveTime);

        // confidentiality code
        clinicalDocument.setConfidentialityCode(DatatypesFactory.eINSTANCE.createCE("N", ""));

        // record target
        RecordTarget recordTarget = CDAFactory.eINSTANCE.createRecordTarget();
        clinicalDocument.getRecordTargets().add(recordTarget);

        // patient role
        PatientRole patientRole = CDAFactory.eINSTANCE.createPatientRole();
        recordTarget.setPatientRole(patientRole);

        // patient
        Patient patient = CDAFactory.eINSTANCE.createPatient();
        patient.setId(DatatypesFactory.eINSTANCE.createII("2.16.840.1.113883.3.933", UUID.randomUUID().toString()));
        patientRole.setPatient(patient);

        // author
        Author author = CDAFactory.eINSTANCE.createAuthor();
        author.getTemplateIds().add(DatatypesFactory.eINSTANCE.createII("2.16.840.1.113883.3.445.2"));
        clinicalDocument.getAuthors().add(author);

        // author function code
        author.setFunctionCode(DatatypesFactory.eINSTANCE.createCE("POACON", "2.16.840.1.113883.1.11.19930"));

        // author time
        author.setTime(DatatypesFactory.eINSTANCE.createIVL_TS(simpleDateFormat.format(new Date())));

        // assigned author
        AssignedAuthor assignedAuthor = CDAFactory.eINSTANCE.createAssignedAuthor();
        assignedAuthor.getIds().add(DatatypesFactory.eINSTANCE.createII("1.3.5.35.1.4436.7", UUID.randomUUID().toString()));
        author.setAssignedAuthor(assignedAuthor);

        // assigned author represented organization
        Organization organization = CDAFactory.eINSTANCE.createOrganization();
        organization.getIds().add(DatatypesFactory.eINSTANCE.createII("1.3.6.4.1.4.1.2835.2", UUID.randomUUID().toString()));
        ON organizationName = DatatypesFactory.eINSTANCE.createON();
        organizationName.addText("ILHIE-SHARPS Prototype");
        organization.getNames().add(organizationName);
        assignedAuthor.setRepresentedOrganization(organization);

        // custodian
        Custodian custodian = CDAFactory.eINSTANCE.createCustodian();
        clinicalDocument.setCustodian(custodian);

        // assigned custodian
        AssignedCustodian assignedCustodian = CDAFactory.eINSTANCE.createAssignedCustodian();
        custodian.setAssignedCustodian(assignedCustodian);

        // custodian organization
        CustodianOrganization custodianOrganization = CDAFactory.eINSTANCE.createCustodianOrganization();
        assignedCustodian.setRepresentedCustodianOrganization(custodianOrganization);
        custodianOrganization.getIds().add(DatatypesFactory.eINSTANCE.createII("1.3.6.4.1.4.1.2835.2", UUID.randomUUID().toString()));
        ON custodianOrganizationName = DatatypesFactory.eINSTANCE.createON();
        custodianOrganizationName.addText("ILHIE-SHARPS Prototype");
        custodianOrganization.setName(custodianOrganizationName);

        // information recipient
        InformationRecipient informationRecipient = CDAFactory.eINSTANCE.createInformationRecipient();
        informationRecipient.setTypeCode(x_InformationRecipient.PRCP);
        clinicalDocument.getInformationRecipients().add(informationRecipient);

        // intended recipient
        IntendedRecipient intendedRecipient = CDAFactory.eINSTANCE.createIntendedRecipient();
        intendedRecipient.setClassCode(x_InformationRecipientRole.ASSIGNED);
        informationRecipient.setIntendedRecipient(intendedRecipient);
        intendedRecipient.getIds().add(DatatypesFactory.eINSTANCE.createII("1.3.6.4.1.4.1.2835.2", UUID.randomUUID().toString()));

        // intended person
        Person person = CDAFactory.eINSTANCE.createPerson();
        intendedRecipient.setInformationRecipient(person);
        person.setClassCode(EntityClass.PSN);
        person.setDeterminerCode(EntityDeterminer.INSTANCE);

        // person name
        PN personName = DatatypesFactory.eINSTANCE.createPN();
        person.getNames().add(personName);
        personName.addText("ILHIE-SHARPS Prototype");

        // received organization
        Organization receivedOrganization = CDAFactory.eINSTANCE.createOrganization();
        intendedRecipient.setReceivedOrganization(receivedOrganization);
        receivedOrganization.setClassCode(EntityClassOrganization.ORG);
        receivedOrganization.setDeterminerCode(EntityDeterminer.INSTANCE);
        II receivedOrganizationId = DatatypesFactory.eINSTANCE.createII("1.3.6.4.1.4.1.2835.2", UUID.randomUUID().toString());
        receivedOrganizationId.setAssigningAuthorityName("NPI");
        receivedOrganization.getIds().add(receivedOrganizationId);
        ON receivedOrganizationName = DatatypesFactory.eINSTANCE.createON();
        receivedOrganizationName.addText("ILHIE-SHARPS Prototype");
        receivedOrganization.getNames().add(receivedOrganizationName);

        // document of
        DocumentationOf documentOf = CDAFactory.eINSTANCE.createDocumentationOf();
        documentOf.setTypeCode(ActRelationshipType.DOC);
        clinicalDocument.getDocumentationOfs().add(documentOf);

        // service event
        ServiceEvent serviceEvent = CDAFactory.eINSTANCE.createServiceEvent();
        documentOf.setServiceEvent(serviceEvent);
        serviceEvent.setMoodCode(ActMood.EVN);
        serviceEvent.getTemplateIds().add(DatatypesFactory.eINSTANCE.createII("2.16.840.1.113883.3.445.3"));
        serviceEvent.getIds().add(DatatypesFactory.eINSTANCE.createII("2.16.840.1.113883.3.445.3", UUID.randomUUID().toString()));
        serviceEvent.setCode(DatatypesFactory.eINSTANCE.createCE("57016-8", "2.16.840.1.113883.6.1"));
        serviceEvent.setEffectiveTime(DatatypesFactory.eINSTANCE.createIVL_TS(simpleDateFormat.format(new Date())));

        // related document
        RelatedDocument relatedDocument = CDAFactory.eINSTANCE.createRelatedDocument();
        relatedDocument.setTypeCode(x_ActRelationshipDocument.RPLC);
        clinicalDocument.getRelatedDocuments().add(relatedDocument);

        // parent document
        ParentDocument parentDocument = CDAFactory.eINSTANCE.createParentDocument();
        parentDocument.setClassCode(ActClinicalDocument.DOCCLIN);
        parentDocument.setMoodCode(ActMood.EVN);
        relatedDocument.setParentDocument(parentDocument);
        parentDocument.getIds().add(DatatypesFactory.eINSTANCE.createII("1.3.6.1.4.1.19376.1.5.3.1.2.6", UUID.randomUUID().toString()));

        Section section = addSectionToClinicalDocument(
                CdaConstants.CONSENT_SECTION_TEMPLATE_ID,
                CdaConstants.CONSENT_SECTION_TEMPLATE_NAME,
                ActClass.DOCSECT,
                ActMood.EVN,
                CdaConstants.CONSENT_SECTION_TEMPLATE_TITLE,
                null,
                null,
                null,
                null,
                clinicalDocument);

        // document component structure config
        Component2 component = clinicalDocument.getComponent();
        component.setTypeCode(ActRelationshipHasComponent.COMP);
        component.setContextConductionInd(Boolean.TRUE);

        Entry entry = SectionUtils.addEntryToSection(
                "2.16.840.1.113883.3.445.4",
                x_ActRelationshipEntry.COMP,
                section);

        Act act = EntryUtils.addActToEntry(
                "2.16.840.1.113883.3.445.5",
                null,
                null,
                x_ActClassDocumentEntryAct.ACT,
                x_DocumentActMood.DEF,
                "TREATMENT",
                "2.16.840.1.113883.3.18.7.1",
                "active",
                entry);

        Informant12 informant = ActUtils.addInformantToAct(
                "2.16.840.1.113883.3.445.6",
                ParticipationType.INF,
                ContextControl.OP,
                act);

        AssignedEntity assignedEntity = InformantUtils.addAssignedEntityToInformant(
                "1.3.6.4.1.4.1.2835.2",
                "980983",
                informant);

        Participant2 participant = ActUtils.addParticipantToAct(
                "2.16.840.1.113883.3.445.7",
                ParticipationType.IRCP,
                ContextControl.OP,
                act);

        ParticipantRole participantRole = ParticipantUtils.addParticipantRoleToParticipant(
                "1.3.5.35.1.4436.7",
                "4564",
                RoleClassRoot.ASSIGNED,
                "ATND",
                "2.16.840.1.113883.11.19682",
                participant);

        EntryRelationship entryRelationship = ActUtils.addEntryRelationshipToAct(
                "2.16.840.1.113883.3.445.8",
                x_ActRelationshipEntryRelationship.COMP,
                Boolean.TRUE,
                act);

        Observation observation = EntryRelationshipUtils.addObservationToEntryRelationship(
                ActClassObservation.OBS,
                x_ActMoodDocumentObservation.DEF,
                Boolean.FALSE,
                "DISCLOSE",
                "2.16.840.1.113883.5.4",
                entryRelationship);

        entryRelationship = ActUtils.addEntryRelationshipToAct(
                "2.16.840.1.113883.3.445.9",
                x_ActRelationshipEntryRelationship.COMP,
                Boolean.TRUE,
                act);

        Organizer organizer = EntryRelationshipUtils.addOrganizerToEntryRelationship(
                x_ActClassDocumentEntryOrganizer.CLUSTER,
                ActMood.DEF,
                "active",
                entryRelationship);

        for (String item : disclosureClassificationList) {

            Component4 component4 = OrganizerUtils.addComponentToOrganizer(
                    ActRelationshipHasComponent.COMP,
                    organizer);

            Observation addObservationToComponent = ComponentUtils.addObservationToComponent(
                    "2.16.840.1.113883.3.445.999",
                    ActClassObservation.OBS,
                    x_ActMoodDocumentObservation.DEF,
                    item,
                    "2.16.840.1.113883.5.4",
                    component4);
        }

        return clinicalDocument;
    }

    /**
     * Initialize a new C32 CDA.
     *
     * @return
     */
    public static ClinicalDocument createC32Document() {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

        ClinicalDocument clinicalDocument = CDAFactory.eINSTANCE.createClinicalDocument();

        // US realm code
        clinicalDocument.getRealmCodes().add(DatatypesFactory.eINSTANCE.createCS("US"));

        // CDA document type ID
        InfrastructureRootTypeId typeId = CDAFactory.eINSTANCE.createInfrastructureRootTypeId();
        typeId.setExtension("POCD_HD000040");
        clinicalDocument.setTypeId(typeId);

        // US General Header Template
        II generalTemplateId = DatatypesFactory.eINSTANCE.createII("2.16.840.1.113883.3.27.1776");
        clinicalDocument.getTemplateIds().add(generalTemplateId);

        // CCD template
        II ccdTemplateId = DatatypesFactory.eINSTANCE.createII("2.16.840.1.113883.10.20.1");
        clinicalDocument.getTemplateIds().add(ccdTemplateId);

        // HITSP/C32 template
        II c32TemplateId = DatatypesFactory.eINSTANCE.createII("2.16.840.1.113883.3.88.11.32.1");
        clinicalDocument.getTemplateIds().add(c32TemplateId);

        // Summarization of Episode Note
        CE code = DatatypesFactory.eINSTANCE.createCE("34133-9", "2.16.840.1.113883.6.1", "LOINC", "Summarization of Episode Note");
        clinicalDocument.setCode(code);

        // title
        ST title = DatatypesFactory.eINSTANCE.createST("Health Summary");
        clinicalDocument.setTitle(title);

        // assign random document ID
        II id = DatatypesFactory.eINSTANCE.createII("2.16.840.1.113883.19.5.99999.1", UUID.randomUUID().toString());
        clinicalDocument.setId(id);

        return clinicalDocument;
    }

    /**
     * Initialize a new Consolidated CDA.
     *
     * @return
     */
    public static ClinicalDocument createConsolDocument() {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

        ClinicalDocument clinicalDocument = CDAFactory.eINSTANCE.createClinicalDocument();

        // US realm code
        clinicalDocument.getRealmCodes().add(DatatypesFactory.eINSTANCE.createCS("US"));

        // CDA document type ID
        InfrastructureRootTypeId typeId = CDAFactory.eINSTANCE.createInfrastructureRootTypeId();
        typeId.setExtension("POCD_HD000040");
        clinicalDocument.setTypeId(typeId);

        // US General Header Template
        II generalTemplateId = DatatypesFactory.eINSTANCE.createII("2.16.840.1.113883.10.20.22.1.1");
        clinicalDocument.getTemplateIds().add(generalTemplateId);

        // CCDA template
        II ccdaTemplateId = DatatypesFactory.eINSTANCE.createII("2.16.840.1.113883.10.20.22.1.2");
        clinicalDocument.getTemplateIds().add(ccdaTemplateId);

        // Summarization of Episode Note
        CE code = DatatypesFactory.eINSTANCE.createCE("34133-9", "2.16.840.1.113883.6.1", "LOINC", "Summarization of Episode Note");
        clinicalDocument.setCode(code);

        // title
        ST title = DatatypesFactory.eINSTANCE.createST("Health Summary");
        clinicalDocument.setTitle(title);

        // assign random document ID
        II id = DatatypesFactory.eINSTANCE.createII("2.16.840.1.113883.19.5.99999.1", UUID.randomUUID().toString());
        clinicalDocument.setId(id);

        return clinicalDocument;
    }

    /**
     * Simple Section creation method with single template ID.
     *
     * @param templateId
     * @param assigningAuthorityName
     * @param title
     * @param code
     * @param clinicalDocument
     * @return
     */
    public static Section addSectionToClinicalDocument(
            String templateId,
            String assigningAuthorityName,
            String title,
            String code,
            ClinicalDocument clinicalDocument) {

        Map<String, String> templateIds = new HashMap<String, String>();
        templateIds.put(templateId, assigningAuthorityName);

        return addSectionToClinicalDocument(
                templateIds,
                null,
                null,
                title,
                code,
                null,
                null,
                null,
                clinicalDocument);
    }

    /**
     * Simple Section creation method with multiple template IDs.
     *
     * @param templateIds
     * @param title
     * @param code
     * @param clinicalDocument
     * @return
     */
    public static Section addSectionToClinicalDocument(
            Map<String, String> templateIds,
            String title,
            String code,
            ClinicalDocument clinicalDocument) {

        return addSectionToClinicalDocument(
                templateIds,
                null,
                null,
                title,
                code,
                null,
                null,
                null,
                clinicalDocument);
    }

    /**
     * Complex Section creation method with single template ID.
     *
     * @param templateId
     * @param assigningAuthorityName
     * @param classCode
     * @param moodCode
     * @param title
     * @param code
     * @param codeSystem
     * @param codeSystemName
     * @param displayName
     * @param clinicalDocument
     * @return
     */
    public static Section addSectionToClinicalDocument(
            String templateId,
            String assigningAuthorityName,
            ActClass classCode,
            ActMood moodCode,
            String title,
            String code,
            String codeSystem,
            String codeSystemName,
            String displayName,
            ClinicalDocument clinicalDocument) {

        Map<String, String> templateIds = new HashMap<String, String>();
        templateIds.put(templateId, assigningAuthorityName);

        return addSectionToClinicalDocument(
                templateIds,
                classCode,
                moodCode,
                title,
                code,
                codeSystem,
                codeSystemName,
                displayName,
                clinicalDocument);
    }

    /**
     * Complex Section creation method with multiple template IDs.
     *
     * @param templateIds
     * @param classCode
     * @param moodCode
     * @param title
     * @param code
     * @param codeSystem
     * @param codeSystemName
     * @param displayName
     * @param clinicalDocument
     * @return
     */
    public static Section addSectionToClinicalDocument(
            Map<String, String> templateIds,
            ActClass classCode,
            ActMood moodCode,
            String title,
            String code,
            String codeSystem,
            String codeSystemName,
            String displayName,
            ClinicalDocument clinicalDocument) {
        final String METHODNAME = "addSectionToClinicalDocument ";
        Section section = CDAFactory.eINSTANCE.createSection();

        if (classCode != null) {
            section.setClassCode(classCode);
        }

        if (moodCode != null) {
            section.setMoodCode(moodCode);
        }

        clinicalDocument.addSection(section);

        // template id
        if (templateIds != null) {
            for (Map.Entry<String, String> entry : templateIds.entrySet()) {
                if (entry.getKey() != null) {
                    II sectionTemplateId = DatatypesFactory.eINSTANCE.createII(entry.getKey());
                    if (entry.getValue() != null) {
                        sectionTemplateId.setAssigningAuthorityName(entry.getValue());
                    }
                    section.getTemplateIds().add(sectionTemplateId);
                }
            }
        }

        // title
        if (title != null) {
            ST sectionTitle = DatatypesFactory.eINSTANCE.createST(title);
            section.setTitle(sectionTitle);
        }

        // code
        if (code != null) {
            if (codeSystem == null) {
                codeSystem = CdaConstants.LOINC_CODE_SYSTEM_OID;
                codeSystemName = CdaConstants.LOINC_CODE_SYSTEM_NAME;
            }
            if (codeSystemName == null) {
                codeSystemName = "";
            }
            if (displayName == null) {
                displayName = "";
            }
            CE sectionCode = DatatypesFactory.eINSTANCE.createCE(code, codeSystem, codeSystemName, displayName);
            section.setCode(sectionCode);
        }
        return section;
    }

    public static void removeSectionFromClinicalDocument(Section section, ClinicalDocument clinicalDocument) {
        final String METHODNAME = "removeSectionFromClinicalDocument ";
        if (section != null) {
            if (clinicalDocument != null) {
                Component2 component = clinicalDocument.getComponent();
                Iterator<Component3> iterator = component.getStructuredBody().getComponents().iterator();
                while (iterator.hasNext()) {
                    Component3 component3 = iterator.next();
                    if (section.equals(component3.getSection())) {
                        iterator.remove();
                        break;
                    }
                }
            } else {
                logger.error(METHODNAME, "clinicalDocument is null!");
            }
        } else {
            logger.error(METHODNAME, "selectedSection is null!");
        }
    }

    public static Map<String, Boolean> getRedactionMap(List<ClinicalDocument> consentClinicalDocuments) {
        final String METHODNAME = "getRedactionMap ";
        Map<String, Boolean> result = new HashMap<String, Boolean>();
        result.put(ILHIE_HIV, Boolean.TRUE);
        result.put(ILHIE_MentalHealth, Boolean.TRUE);

        for (ClinicalDocument consentClinicalDocument : consentClinicalDocuments) {
            for (CD code : getDisclosureList(consentClinicalDocument)) {
                if (code != null) {
                    if (ILHIE_HIV.equalsIgnoreCase(code.getCode())) {
                        logger.info("Found directive to disclose HIV related data.");
                        result.put(ILHIE_HIV, Boolean.FALSE);
                    } else if (ILHIE_MentalHealth.equalsIgnoreCase(code.getCode())) {
                        logger.info("Found directive to disclose Mental Health related data.");
                        result.put(ILHIE_MentalHealth, Boolean.FALSE);
                    } else if (ILHIE_SubstanceAbuse.equalsIgnoreCase(code.getCode())) {
                        logger.info("Found directive to disclose Substance Abuse related data.");
                        result.put(ILHIE_SubstanceAbuse, Boolean.FALSE);
                    }
                } else {
                    logger.error(METHODNAME, "code is null!");
                }
            }
        }
        logger.info(METHODNAME, "redactionMap: ", result);
        return result;
    }

    public static boolean isConsentDocument(ClinicalDocument clinicalDocument) {
        final String METHODNAME = "isConsentDocument ";
        boolean result = false;
        if (clinicalDocument != null) {
            logger.info(METHODNAME, clinicalDocument.getTemplateIds());
            if (clinicalDocument.getTemplateIds() != null) {
                for (II ii : clinicalDocument.getTemplateIds()) {
                    if (ii.getRoot().equalsIgnoreCase("2.16.840.1.113883.3.445.1")) {
                        result = true;
                        break;
                    }
                }
            } else {
                logger.error(METHODNAME, "clinicalDocument.getTemplateIds() is null!");
            }
        } else {
            logger.error(METHODNAME, "clinicalDocument is null!");
        }
        return result;
    }

    /**
     * Returns a list of disclosed codes from the supplied consent clinical document.
     *
     * @param clinicalDocument
     * @return
     */
    public static List<CD> getDisclosureList(ClinicalDocument clinicalDocument) {

        final String METHODNAME = "getDisclosureList ";
        List<CD> disclosureList = new ArrayList<CD>();
        long start = System.nanoTime();
        if (clinicalDocument != null) {
            if (isConsentDocument(clinicalDocument)) {
                for (Section section : clinicalDocument.getAllSections()) {
                    for (Entry entry : section.getEntries()) {
                        for (EntryRelationship entryRelationship : entry.getAct().getEntryRelationships()) {
                            if (entryRelationship.getOrganizer() != null) {
                                for (Component4 component : entryRelationship.getOrganizer().getComponents()) {
                                    if (component.getObservation() != null) {
                                        if (component.getObservation().getCode() != null) {
                                            disclosureList.add(component.getObservation().getCode());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                logger.error(METHODNAME, "clinicalDocument is not a consent document!");
            }
        } else {
            logger.error(METHODNAME, "clinicalDocument is null!");
        }
        logger.logDuration(METHODNAME, start);
        return disclosureList;
    }
}

