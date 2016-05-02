package open.dolphin.session;

import java.text.SimpleDateFormat;
import java.util.*;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import open.dolphin.common.util.ModuleBeanDecoder;
import open.dolphin.infomodel.*;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;

/**
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author modified by masuda, Masuda Naika
 * @author modified by katoh, Hashimoto iin
 */
@Stateless
public class KarteServiceBean {

    private static final String FID = "fid";
    private static final String PID = "pid";
    private static final String ID = "id";
    private static final String ENTITY = "entity";
    private static final String KARTE_ID = "karteId";
    private static final String FROM_DATE = "fromDate";
    private static final String TO_DATE = "toDate";
    private static final String PATIENT_PK = "patientPk";
    
    private static final String QUERY_KARTE 
            = "from KarteBean k where k.patient.id=:patientPk";
    private static final String QUERY_ALLERGY 
            = "from ObservationModel o where o.karte.id=:karteId and o.observation='Allergy'";
    private static final String QUERY_BODY_HEIGHT 
            = "from ObservationModel o where o.karte.id=:karteId and o.observation='PhysicalExam' and o.phenomenon='bodyHeight'";
    private static final String QUERY_BODY_WEIGHT 
            = "from ObservationModel o where o.karte.id=:karteId and o.observation='PhysicalExam' and o.phenomenon='bodyWeight'";
    private static final String QUERY_PATIENT_VISIT 
            = "from PatientVisitModel p where p.patient.id=:patientPk and p.pvtDate >= :fromDate";
    private static final String QUERY_DOC_INFO 
            = "from DocumentModel d where d.karte.id=:karteId and d.started >= :fromDate and (d.status='F' or d.status='T')";
    private static final String QUERY_PATIENT_MEMO 
            = "from PatientMemoModel p where p.karte.id=:karteId";

    //private static final String QUERY_DOCUMENT_INCLUDE_MODIFIED 
    //        = "from DocumentModel d where d.karte.id=:karteId and d.started >= :fromDate and d.status !='D'";
    //private static final String QUERY_DOCUMENT 
    //        = "from DocumentModel d where d.karte.id=:karteId and d.started >= :fromDate and (d.status='F' or d.status='T')";
    private static final String QUERY_DOCUMENT_BY_LINK_ID 
            = "from DocumentModel d where d.linkId=:id";

    private static final String QUERY_MODULE_BY_DOC_ID 
            = "from ModuleModel m where m.document.id=:id";
    private static final String QUERY_SCHEMA_BY_DOC_ID 
            = "from SchemaModel i where i.document.id=:id";
    private static final String QUERY_MODULE_BY_ENTITY 
            = "from ModuleModel m where m.karte.id=:karteId and m.moduleInfo.entity=:entity and m.started between :fromDate and :toDate and m.status='F'";
    private static final String QUERY_SCHEMA_BY_KARTE_ID 
            = "from SchemaModel i where i.karte.id =:karteId and i.started between :fromDate and :toDate and i.status='F'";

    private static final String QUERY_SCHEMA_BY_FACILITY_ID 
            = "from SchemaModel i where i.karte.patient.facilityId like :fid and i.extRef.sop is not null and i.status='F'";

    private static final String QUERY_DIAGNOSIS_BY_KARTE_DATE 
            = "from RegisteredDiagnosisModel r where r.karte.id=:karteId and r.started >= :fromDate";
    private static final String QUERY_DIAGNOSIS_BY_KARTE_DATE_ACTIVEONLY 
            = "from RegisteredDiagnosisModel r where r.karte.id=:karteId and r.started >= :fromDate and r.ended is NULL";
    private static final String QUERY_DIAGNOSIS_BY_KARTE 
            = "from RegisteredDiagnosisModel r where r.karte.id=:karteId";
    private static final String QUERY_DIAGNOSIS_BY_KARTE_ACTIVEONLY 
            = "from RegisteredDiagnosisModel r where r.karte.id=:karteId and r.ended is NULL";
    
    private static final String QUERY_APPO_BY_KARTE_ID_PERIOD
            = "from AppointmentModel a where a.karte.id = :karteId and a.date between :fromDate and :toDate";

    private static final String QUERY_PATIENT_BY_FID_PID 
            = "from PatientModel p where p.facilityId=:fid and p.patientId=:pid";

//masuda^
    private static final String QUERY_LASTDOC_DATE 
            = "select max(m.started) from DocumentModel m where m.karte.id = :karteId and (m.status = 'F' or m.status = 'T')";
    private static final String QUERY_APPOINTMENTS 
            = "from AppointmentModel a where a.karte.id = :karteId and a.started >= :fromDate";
    private static final String QUERY_DOCUMENT_INCLUDE_MODIFIED 
            = "from DocumentModel d where d.karte.id=:karteId and d.started >= :fromDate and d.started < :toDate";
    private static final String QUERY_DOCUMENT 
            = "from DocumentModel d where d.karte.id=:karteId and d.started >= :fromDate and d.started < :toDate "
            + "and (d.status='F' or d.status='T')";
    private static final String QUERY_SUMMARY
            = "from DocumentModel d where d.karte.id=:karteId and d.docInfo.docType = "
            + "'" + IInfoModel.DOCTYPE_SUMMARY + "' and (d.status='F' or d.status='T') order by d.started desc";
    private static final String QUERY_SOA_SPEC
            = "from ModuleModel m where m.document.id = :id "
            + "and m.moduleInfo.role = 'soaSpec' and m.moduleInfo.name = 'progressCourse'";
//masuda$

    @PersistenceContext
    private EntityManager em;
    
    
    public KarteBean getKarte(String fid, String pid, Date fromDate) {
        
        try {
            // 患者レコードは FacilityId と patientId で複合キーになっている
            PatientModel patient
                = (PatientModel)em.createQuery(QUERY_PATIENT_BY_FID_PID)
                .setParameter(FID, fid)
                .setParameter(PID, pid)
                .getSingleResult();
            
//masuda    下のgetKarteにまとめる
            return getKarte(patient.getId(), fromDate);

        } catch (Exception e) {
        }
        
        return null;
    }

    /**
     * カルテの基礎的な情報をまとめて返す。
     * @param patientPk 患者の Database Primary Key
     * @param fromDate 各種エントリの検索開始日
     * @return 基礎的な情報をフェッチした KarteBean
     */
    public KarteBean getKarte(long patientPK, Date fromDate) {

        try {
            // 最初に患者のカルテを取得する
            List<KarteBean> kartes = 
                    em.createQuery(QUERY_KARTE)
                    .setParameter(PATIENT_PK, patientPK)
                    .getResultList();
            KarteBean karte = kartes.get(0);

            // カルテの PK を得る
            long karteId = karte.getId();

            // アレルギーデータを取得する
            List<ObservationModel> list1 = 
                    em.createQuery(QUERY_ALLERGY)
                    .setParameter(KARTE_ID, karteId)
                    .getResultList();
            if (!list1.isEmpty()) {
                List<AllergyModel> allergies = new ArrayList<>(list1.size());
                for (ObservationModel observation : list1) {
                    AllergyModel allergy = new AllergyModel();
                    allergy.setObservationId(observation.getId());
                    allergy.setFactor(observation.getPhenomenon());
                    allergy.setSeverity(observation.getCategoryValue());
                    allergy.setIdentifiedDate(observation.confirmDateAsString());
                    allergy.setMemo(observation.getMemo());
                    allergies.add(allergy);
                }
                karte.setAllergies(allergies);
            }

            // 身長データを取得する
            List<ObservationModel> list2 = 
                    em.createQuery(QUERY_BODY_HEIGHT)
                    .setParameter(KARTE_ID, karteId)
                    .getResultList();
            if (!list2.isEmpty()) {
                List<PhysicalModel> physicals = new ArrayList<>(list2.size());
                for (ObservationModel observation : list2) {
                    PhysicalModel physical = new PhysicalModel();
                    physical.setHeightId(observation.getId());
                    physical.setHeight(observation.getValue());
                    physical.setIdentifiedDate(observation.confirmDateAsString());
                    physical.setMemo(ModelUtils.getDateAsString(observation.getRecorded()));
                    physicals.add(physical);
                }
                karte.setHeights(physicals);
            }

            // 体重データを取得する
            List<ObservationModel> list3 = 
                    em.createQuery(QUERY_BODY_WEIGHT)
                    .setParameter(KARTE_ID, karteId)
                    .getResultList();
            if (!list3.isEmpty()) {
                List<PhysicalModel> physicals = new ArrayList<>(list3.size());
                for (ObservationModel observation : list3) {
                    PhysicalModel physical = new PhysicalModel();
                    physical.setWeightId(observation.getId());
                    physical.setWeight(observation.getValue());
                    physical.setIdentifiedDate(observation.confirmDateAsString());
                    physical.setMemo(ModelUtils.getDateAsString(observation.getRecorded()));
                    physicals.add(physical);
                }
                karte.setWeights(physicals);
            }
//masuda^   使わない
/*
            // 直近の来院日エントリーを取得しカルテに設定する
            List<PatientVisitModel> latestVisits = 
                    em.createQuery(QUERY_PATIENT_VISIT)
                    .setParameter(PATIENT_PK, patientPK)
                    .setParameter(FROM_DATE, ModelUtils.getDateAsString(fromDate))
                    .getResultList();

            if (!latestVisits.isEmpty()) {
                List<String> visits = new ArrayList<String>(latestVisits.size());
                for (PatientVisitModel bean : latestVisits) {
                    // 来院日のみを使用する
                    visits.add(bean.getPvtDate());
                }
                karte.setPatientVisits(visits);
            }

//masuda    後で取得する
            // 文書履歴エントリーを取得しカルテに設定する
            List<DocumentModel> documents = 
                    em.createQuery(QUERY_DOC_INFO)
                    .setParameter(KARTE_ID, karteId)
                    .setParameter(FROM_DATE, fromDate)
                    .getResultList();

            if (!documents.isEmpty()) {
                List<DocInfoModel> c = new ArrayList<DocInfoModel>(documents.size());
                for (DocumentModel docBean : documents) {
                    docBean.toDetuch();
                    c.add(docBean.getDocInfoModel());
                }
                karte.setDocInfoList(c);
            }
*/
//masuda$
            // 患者Memoを取得する
            List<PatientMemoModel> memo = 
                    em.createQuery(QUERY_PATIENT_MEMO)
                    .setParameter(KARTE_ID, karteId)
                    .getResultList();
            if (!memo.isEmpty()) {
                karte.setMemoList(memo);
            }
//masuda^
            // 最終文書日
            try {
                Date lastDocDate = (Date)
                        em.createQuery(QUERY_LASTDOC_DATE)
                        .setParameter(KARTE_ID, karteId)
                        .getSingleResult();
                karte.setLastDocDate(lastDocDate);
            } catch (NoResultException e) {
            }
            // 予約
            List<AppointmentModel> appoList =
                    em.createQuery(QUERY_APPOINTMENTS)
                    .setParameter(KARTE_ID, karteId)
                    .setParameter(FROM_DATE, fromDate)
                    .getResultList();
            if (appoList != null && !appoList.isEmpty()) {
                karte.setAppointmentList(appoList);
            }
            // サマリー
            List<DocumentModel> summaryList = 
                    em.createQuery(QUERY_SUMMARY)
                    .setParameter(KARTE_ID, karteId)
                    .setMaxResults(1)
                    .getResultList();
            
            if (summaryList != null && !summaryList.isEmpty()) {
                DocumentModel summary = summaryList.get(0);
                long id = summary.getId();
                // progressCourse, soaSpecのみを取得する
                List modules = em.createQuery(QUERY_SOA_SPEC)
                        .setParameter(ID, id)
                        .getResultList();
                summary.setModules(modules);
                // SchemaModelは空にする
                List<SchemaModel> schemas = Collections.emptyList();
                summary.setSchema(schemas);
                
                karte.setSummary(summary);
            }
//masuda$
            return karte;

        } catch (NoResultException e) {
            // 患者登録の際にカルテも生成してある
        }

        return null;
    }

    /**
     * 文書履歴エントリを取得する。
     * @param karteId カルテId
     * @param fromDate 取得開始日
     * @param status ステータス
     * @return DocInfo のコレクション
     */
    public List<DocInfoModel> getDocumentList(long karteId, Date fromDate, Date toDate, boolean includeModifid) {

        List<DocumentModel> documents;

        if (includeModifid) {
//masuda, katoh^
            //documents = (List<DocumentModel>)em.createQuery(QUERY_DOCUMENT_INCLUDE_MODIFIED)
            documents = 
                    em.createQuery(QUERY_DOCUMENT_INCLUDE_MODIFIED)
                    .setParameter(KARTE_ID, karteId)
                    .setParameter(FROM_DATE, fromDate)
                    .setParameter(TO_DATE, toDate)
                    .getResultList();
        } else {
            documents = 
                    em.createQuery(QUERY_DOCUMENT)
                    .setParameter(KARTE_ID, karteId)
                    .setParameter(FROM_DATE, fromDate)
                    .setParameter(TO_DATE, toDate)
                    .getResultList();
        }
        // 最新のサマリーは必ず含める
        List<DocumentModel> summaries =
                em.createQuery(QUERY_SUMMARY)
                .setParameter(KARTE_ID, karteId)
                .setMaxResults(1)
                .getResultList();
        
        Set<DocumentModel> docSet = new HashSet<>();
        docSet.addAll(documents);
        docSet.addAll(summaries);

        List<DocInfoModel> result = new ArrayList<>();
        
        for (DocumentModel doc : docSet) {
            // モデルからDocInfo へ必要なデータを移す
            // クライアントが DocInfo だけを利用するケースがあるため
            doc.toDetuch();
            result.add(doc.getDocInfoModel());
            
            // find root user
            DocumentModel parent = getParent(doc);
            doc.getDocInfoModel().setRootUser(parent.getUserModel().getCommonName());
        }
//masuda, katoh$
        
        return result;
}
    
//masuda^
    /**
     * 文書(DocumentModel Object)を取得する。
     * @param ids DocumentModel の pkコレクション
     * @return DocumentModelのコレクション
     */
    public List<DocumentModel> getDocuments(List<Long> ids) {

        List<DocumentModel> documentList =
                em.createQuery("from DocumentModel m where m.id in (:ids)")
                .setParameter("ids", ids)
                .getResultList();
        
        // Lazy fetchのやつらを取得するtrick
        for (DocumentModel dm : documentList) {
            // サイズを取得するだけでfetchできる
            dm.getModules().size();
            dm.getSchema().size();
        }

        return documentList;
    }
    
    /**
     * ドキュメント DocumentModel オブジェクトを保存する。
     * @param document 追加するDocumentModel オブジェクト
     * @return Document.id
     */
    public long addDocument(DocumentModel document) {

        // 永続化する
        em.persist(document);
        long id = document.getId();
        
        // 修正版の処理と算定履歴登録は非同期処理させる
        processPostAddDocument(document);

        // Document.idを返す
        return id;
    }
    
    @Asynchronous
    private void processPostAddDocument(DocumentModel document) {
        
        // 算定履歴を登録する
        registSanteiHistory(document);

        // 修正版の処理を行う
        DocInfoModel docInfo = document.getDocInfoModel();
        long parentPk = docInfo.getParentPk();

        // 親がないならリターン
        if (parentPk == 0L) {
            return;
        }
        DocumentModel old = em.find(DocumentModel.class, parentPk);
        if (old == null) {
            return;
        }
        
        // 親文書が仮保存文書なら残す必要なし。なぜならそれは仮保存だから。
        if (IInfoModel.STATUS_TMP.equals(old.getStatus())) {
            // 編集元文書の情報を引き継ぐ
            DocInfoModel pInfo = old.getDocInfoModel();
            document.setLinkId(old.getLinkId());
            document.setLinkRelation(old.getLinkRelation());
            //docInfo.setParentPk(pInfo.getParentPk());   // parentPk = linkId
            docInfo.setParentId(pInfo.getParentId());
            docInfo.setParentIdRelation(pInfo.getParentIdRelation());
            docInfo.setVersionNumber(pInfo.getVersionNumber());
            
            // 算定履歴も削除。こっちが先
            deleteSanteiHistory(parentPk);
            
            // 編集元は削除
            em.remove(old);

        } else {
            // 適合終了日を新しい版の確定日にする
            Date ended = document.getConfirmed();
            // 終了日と status = M を設定する
            old.setEnded(ended);
            old.setStatus(IInfoModel.STATUS_MODIFIED);

            // HibernateSearchのFulTextEntityManagerを用意。修正済みのものはインデックスから削除する
            final FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);
            fullTextEntityManager.purge(DocumentModel.class, parentPk);

            // 関連するモジュールとイメージに同じ処理を実行する
            List<ModuleModel> oldModules =
                    em.createQuery(QUERY_MODULE_BY_DOC_ID)
                    .setParameter(ID, parentPk)
                    .getResultList();
            for (ModuleModel model : oldModules) {
                model.setEnded(ended);
                model.setStatus(IInfoModel.STATUS_MODIFIED);
            }

            List<SchemaModel> oldImages =
                    em.createQuery(QUERY_SCHEMA_BY_DOC_ID)
                    .setParameter(ID, parentPk)
                    .getResultList();
            for (SchemaModel model : oldImages) {
                model.setEnded(ended);
                model.setStatus(IInfoModel.STATUS_MODIFIED);
            }
            
            // 修正されたものは算定履歴から削除する
            deleteSanteiHistory(parentPk);      
        }
    }

    /**
     * ドキュメントを論理削除する。
     * @param pk 論理削除するドキュメントの primary key
     * @return 削除した件数
     */
    public int deleteDocument(long id) {
        
        // オリジナルでは修正したり仮保存をした文書を削除できないので改変
        
        // 削除対象のDocumentModelを取得
        DocumentModel target = em.find(DocumentModel.class, id);
        // その親文書を取得
        DocumentModel parent = getParent(target);
        // 関連するDocumentModelを再帰で取得する
        Set<DocumentModel> delSet = getChildren(parent);
        
        Date ended = new Date();
        Boolean     bIsFinalUnique = true;
        
        for (DocumentModel delete: delSet) {
            if (delete.getId() != id && delete.getStatus().equals(IInfoModel.STATUS_FINAL)){
                // 同一のparentの文書で、かつSTATUS_FINALが指定されている文書が２つ以上ある場合は記録しておき、
                // 指定された文書だけを削除する (2013.11.28 katou)
                bIsFinalUnique = false;
            }
        }
        
        if (bIsFinalUnique == true){
        for (DocumentModel delete : delSet) {
                // STATUS_FINALが単一なので、同一parentのDocumentすべてにDELETEマークをつける
                DeleteOneDocument(delete.getId(), delete, ended);
            }
        }
        else{
            // STATUS_FINALが複数あるので、指定されたDocumentのみにDELETEマークをつける
            DeleteOneDocument(id, target, ended);
        }
            
        return 1;
    }
            
    private void DeleteOneDocument(long id, DocumentModel target, Date ended){
            // HibernateSearchのFulTextEntityManagerを用意。削除済みのものはインデックスから削除する
            final FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);
        fullTextEntityManager.purge(DocumentModel.class, id);
            
            // 削除するものは算定履歴も削除する
        deleteSanteiHistory(id);

        if (IInfoModel.STATUS_TMP.equals(target.getStatus())) {
                // 仮文書の場合は抹消スル
            DocumentModel dm = em.find(DocumentModel.class, id);
                em.remove(dm);
                
            } else {
                // 削除フラグをたてる
            target.setStatus(IInfoModel.STATUS_DELETE);
            target.setEnded(ended);

                // 関連するモジュールに同じ処理を行う
                List<ModuleModel> deleteModules =
                        em.createQuery(QUERY_MODULE_BY_DOC_ID)
                    .setParameter(ID, id)
                        .getResultList();
                for (ModuleModel model : deleteModules) {
                    model.setStatus(IInfoModel.STATUS_DELETE);
                    model.setEnded(ended);
                }

                // 関連する画像に同じ処理を行う
                List<SchemaModel> deleteImages =
                        em.createQuery(QUERY_SCHEMA_BY_DOC_ID)
                    .setParameter(ID, id)
                        .getResultList();
                for (SchemaModel model : deleteImages) {
                    model.setStatus(IInfoModel.STATUS_DELETE);
                    model.setEnded(ended);
                }
            }
        }
//masuda^
    // 親分文書を追いかける
    public DocumentModel getParent(DocumentModel dm) {

        DocumentModel model = dm;
        
        while (model.getLinkId() != 0) {
            DocumentModel parent = em.find(DocumentModel.class, model.getLinkId());
            if (parent == null) {
                System.out.println(String.format("Invalid linkId: docPk=%d, linkId=%d",
                        model.getId(), model.getLinkId()));
                break;
            }
            model = parent;
        }
        
        return model;
    }
    
    // 子文書を再帰で探す
    public Set<DocumentModel> getChildren(DocumentModel parent) {
        
        Set<DocumentModel> ret = new HashSet<>();
        
        // 親を追加
        ret.add(parent);

        List<DocumentModel> children = 
                em.createQuery(QUERY_DOCUMENT_BY_LINK_ID)
                .setParameter(ID, parent.getId())
                .getResultList();
        
        // 子供の子供をリストに追加
        for (DocumentModel child : children) {
            ret.addAll(getChildren(child));
        }
        
        return ret;
    }
//masuda$
    
    /**
     * ドキュメントのタイトルを変更する。
     * @param pk 変更するドキュメントの primary key
     * @return 変更した件数
     */
    public int updateTitle(long pk, String title) {
        DocumentModel update = em.find(DocumentModel.class, pk);
        update.getDocInfoModel().setTitle(title);
        return 1;
    }

    /**
     * ModuleModelエントリを取得する。
     * @param spec モジュール検索仕様
     * @return ModuleModelリストのリスト
     */
    
    public List<List<ModuleModel>> getModules(long karteId, String entity, List<Date> fromDate, List<Date> toDate) {

        // 抽出期間は別けられている
        int len = fromDate.size();
        List<List<ModuleModel>> ret = new ArrayList<>(len);

        // 抽出期間セットの数だけ繰り返す
        for (int i = 0; i < len; i++) {

            List<ModuleModel> modules = 
                    em.createQuery(QUERY_MODULE_BY_ENTITY)
                    .setParameter(KARTE_ID, karteId)
                    .setParameter(ENTITY, entity)
                    .setParameter(FROM_DATE, fromDate.get(i))
                    .setParameter(TO_DATE, toDate.get(i))
                    .getResultList();

            ret.add(modules);
        }

        return ret;
    }

    /**
     * SchemaModelエントリを取得する。
     * @param karteId カルテID
     * @param fromDate
     * @param toDate
     * @return SchemaModelエントリの配列
     */
    public List<List<SchemaModel>> getImages(long karteId, List<Date> fromDate, List<Date> toDate) {

        // 抽出期間は別けられている
        int len = fromDate.size();
        List<List<SchemaModel>> ret = new ArrayList<>(len);

        // 抽出期間セットの数だけ繰り返す
        for (int i = 0; i < len; i++) {

            List<SchemaModel> modules = 
                    em.createQuery(QUERY_SCHEMA_BY_KARTE_ID)
                    .setParameter(KARTE_ID, karteId)
                    .setParameter(FROM_DATE, fromDate.get(i))
                    .setParameter(TO_DATE, toDate.get(i))
                    .getResultList();

            ret.add(modules);
        }

        return ret;
    }

    /**
     * 画像を取得する。
     * @param id SchemaModel Id
     * @return SchemaModel
     */
    public SchemaModel getImage(long id) {
        SchemaModel image = em.find(SchemaModel.class, id);
        return image;
    }
/*
    public List<SchemaModel> getS3Images(String fid, int firstResult, int maxResult) {

        List<SchemaModel> ret = 
                em.createQuery(QUERY_SCHEMA_BY_FACILITY_ID)
                .setParameter(FID, fid+"%")
                .setFirstResult(firstResult)
                .setMaxResults(maxResult)
                .getResultList();
        
        return ret;
    }

    public void deleteS3Image(long pk) {
        SchemaModel target = em.find(SchemaModel.class, pk);
        target.getExtRefModel().setBucket(null);
        target.getExtRefModel().setSop(null);
        target.getExtRefModel().setUrl(null);
    }
*/
    /**
     * 傷病名リストを取得する。
     * @param spec 検索仕様
     * @return 傷病名のリスト
     */
    public List<RegisteredDiagnosisModel> getDiagnosis(long karteId, Date fromDate, boolean activeOnly) {

        List<RegisteredDiagnosisModel> ret;

        // 疾患開始日を指定している
        if (fromDate != null) {
            String query = activeOnly ? QUERY_DIAGNOSIS_BY_KARTE_DATE_ACTIVEONLY : QUERY_DIAGNOSIS_BY_KARTE_DATE;
            ret =  em.createQuery(query)
                    .setParameter(KARTE_ID, karteId)
                    .setParameter(FROM_DATE, fromDate)
                    .getResultList();
        } else {
            // 全期間の傷病名を得る
            String query = activeOnly ? QUERY_DIAGNOSIS_BY_KARTE_ACTIVEONLY : QUERY_DIAGNOSIS_BY_KARTE;
            ret = em.createQuery(query)
                    .setParameter(KARTE_ID, karteId)
                    .getResultList();
        }

        return ret;
    }

    /**
     * 傷病名を追加する。
     * @param addList 追加する傷病名のリスト
     * @return idのリスト
     */
    public List<Long> addDiagnosis(List<RegisteredDiagnosisModel> addList) {

        List<Long> ret = new ArrayList<>(addList.size());

        for (RegisteredDiagnosisModel bean : addList) {
            em.persist(bean);
            ret.add(new Long(bean.getId()));
        }

        return ret;
    }

    /**
     * 傷病名を更新する。
     * @param updateList
     * @return 更新数
     */
    public int updateDiagnosis(List<RegisteredDiagnosisModel> updateList) {

        int cnt = 0;

        for (RegisteredDiagnosisModel bean : updateList) {
            em.merge(bean);
            cnt++;
        }

        return cnt;
    }

    /**
     * 傷病名を削除する。
     * @param removeList 削除する傷病名のidリスト
     * @return 削除数
     */
    public int removeDiagnosis(List<Long> removeList) {

        int cnt = 0;

        for (Long id : removeList) {
            RegisteredDiagnosisModel bean = em.find(RegisteredDiagnosisModel.class, id);
            if (bean != null) {
                em.remove(bean);
                cnt++;
            }
        }

        return cnt;
    }

    /**
     * Observationを取得する。
     * @param spec 検索仕様
     * @return Observationのリスト
     */
    public List<ObservationModel> getObservations(long karteId, String observation, String phenomenon, Date firstConfirmed) {

        List<ObservationModel> ret = null;

        if (observation != null) {
            if (firstConfirmed != null) {
                ret = em.createQuery("from ObservationModel o where o.karte.id=:karteId and o.observation=:observation and o.started >= :firstConfirmed")
                .setParameter(KARTE_ID, karteId)
                .setParameter("observation", observation)
                .setParameter("firstConfirmed", firstConfirmed)
                .getResultList();

            } else {
                ret = em.createQuery("from ObservationModel o where o.karte.id=:karteId and o.observation=:observation")
                .setParameter(KARTE_ID, karteId)
                .setParameter("observation", observation)
                .getResultList();
            }
        } else if (phenomenon != null) {
            if (firstConfirmed != null) {
                ret = em.createQuery("from ObservationModel o where o.karte.id=:karteId and o.phenomenon=:phenomenon and o.started >= :firstConfirmed")
                .setParameter(KARTE_ID, karteId)
                .setParameter("phenomenon", phenomenon)
                .setParameter("firstConfirmed", firstConfirmed)
                .getResultList();
            } else {
                ret = em.createQuery("from ObservationModel o where o.karte.id=:karteId and o.phenomenon=:phenomenon")
                .setParameter(KARTE_ID, karteId)
                .setParameter("phenomenon", phenomenon)
                .getResultList();
            }
        }
        return ret;
    }

    /**
     * Observationを追加する。
     * @param observations 追加するObservationのリスト
     * @return 追加したObservationのIdリスト
     */
    public List<Long> addObservations(List<ObservationModel> observations) {

        if (observations != null && observations.size() > 0) {

            List<Long> ret = new ArrayList<>(observations.size());

            for (ObservationModel model : observations) {
                em.persist(model);
                ret.add(new Long(model.getId()));
            }

            return ret;
        }
        return null;
    }

    /**
     * Observationを削除する。
     * @param observations 削除するObservationのリスト
     * @return 削除した数
     */
    public int removeObservations(List<Long> observations) {
        if (observations != null && observations.size() > 0) {
            int cnt = 0;
            for (Long id : observations) {
                ObservationModel model = em.find(ObservationModel.class, id);
                if (model != null) {
                    em.remove(model);
                    cnt++;
                }
            }
            return cnt;
        }
        return 0;
    }

    /**
     * 患者メモを更新する。
     * @param memo 更新するメモ
     */
    public int updatePatientMemo(PatientMemoModel memo) {

        int cnt = 0;

        if (memo.getId() == 0L) {
            em.persist(memo);
        } else {
            em.merge(memo);
        }
        cnt++;
        return cnt;
    }

    //--------------------------------------------------------------------------

    public List<List<AppointmentModel>> getAppointmentList(long karteId, List<Date> fromDate, List<Date> toDate) {

        // 抽出期間は別けられている
        int len = fromDate.size();
        List<List<AppointmentModel>> ret = new ArrayList<>(len);

        // 抽出期間セットの数だけ繰り返す
        for (int i = 0; i < len; i++) {

            List<AppointmentModel> modules = 
                    em.createQuery(QUERY_APPO_BY_KARTE_ID_PERIOD)
                    .setParameter(KARTE_ID, karteId)
                    .setParameter(FROM_DATE, fromDate.get(i))
                    .setParameter(TO_DATE, toDate.get(i))
                    .getResultList();

            ret.add(modules);
        }

        return ret;
    }

    
//masuda^   算定情報登録
    private void registSanteiHistory(DocumentModel document) {
        
        // 算定履歴登録はFINALカルテのみ
        if (!IInfoModel.STATUS_FINAL.equals(document.getStatus())) {
            return;
        }
        
        List<ModuleModel> mmList = document.getModules();
        Date date = document.getStarted();
        
        // 保存するDocumentで電子点数表に関連のあるsrycdを取得する
        Set<String> srycdSet = new HashSet<>();
        for (ModuleModel mm : mmList) {
            String entity = mm.getModuleInfoBean().getEntity();
            if (IInfoModel.MODULE_PROGRESS_COURSE.equals(entity)) {
                continue;
            }
            //mm.setModel((IModuleModel) BeanUtils.xmlDecode(mm.getBeanBytes()));
            mm.setModel(ModuleBeanDecoder.getInstance().decode(mm.getBeanBytes()));
            ClaimBundle cb = (ClaimBundle) mm.getModel();
            if (cb == null) {
                continue;
            }
            for (ClaimItem ci : cb.getClaimItem()) {
                srycdSet.add(ci.getCode());
            }
        }
        // 空ならリターン
        if (srycdSet.isEmpty()) {
            return;
        }
        List<String> srycdList = getETenRelatedSrycdList(date, srycdSet);
        
        // 電子点数表に関連のあるものは算定履歴に登録する。
        for (ModuleModel mm : mmList) {
            String entity = mm.getModuleInfoBean().getEntity();
            if (IInfoModel.MODULE_PROGRESS_COURSE.equals(entity)) {
                continue;
            }
            ClaimBundle cb = (ClaimBundle) mm.getModel();
            if (cb == null) {
                continue;
            }
            int bundleNumber = parseInt(cb.getBundleNumber());
            for (int i = 0; i < cb.getClaimItem().length; ++i) {
                ClaimItem ci = cb.getClaimItem()[i];
                if (!srycdList.contains(ci.getCode())) {
                    continue;
                }
                int claimNumber = parseInt(ci.getNumber());
                int count = bundleNumber * claimNumber;
                SanteiHistoryModel history = new SanteiHistoryModel();
                history.setSrycd(ci.getCode());
                history.setItemCount(count);
                history.setItemIndex(i);
                history.setModuleModel(mm);
                em.persist(history);
            }
        }
    }
    
    private int parseInt(String str) {

        int num = 1;
        try {
            num = Integer.valueOf(str);
        } catch (NumberFormatException e) {
        }
        return num;
    }
    
    // 指定されたidのDocumentModelに関連するSanteiHistoryModelを削除する
    private void deleteSanteiHistory(long docPk) {
        
        if (docPk == 0L) {
            return;
        }
        
        final String sql = "delete from SanteiHistoryModel s where s.moduleModel.id in (:mIds)";

        DocumentModel document = em.find(DocumentModel.class, docPk);
        
        List<Long> mIds = new ArrayList<>();
        
        for (ModuleModel mm : document.getModules()) {
            mIds.add(mm.getId());
        }
        if (!mIds.isEmpty()) {
            em.createQuery(sql).setParameter("mIds", mIds).executeUpdate();
        }
    }
    
    private List<String> getETenRelatedSrycdList(Date date, Collection<String> srycds) {

        final String sql1 = "select distinct e.srycd from ETensuModel1 e";
        final String sql2 = sql1 + " where e.yukostymd <= :date and :date <= e.yukoedymd";
        final String sql3 = sql2 + " and e.srycd in (:srycds)";
        final SimpleDateFormat frmt2 = new SimpleDateFormat("yyyyMMdd");
        
        List<String> list;
        if (date == null && srycds == null) {
            list = em.createQuery(sql1).getResultList();
        } else {
            String ymd = frmt2.format(date);
            if (srycds == null) {
                list = em.createQuery(sql2)
                        .setParameter("date", ymd)
                        .getResultList();
            } else {
                list = em.createQuery(sql3)
                        .setParameter("date", ymd)
                        .setParameter("srycds", srycds)
                        .getResultList();
            }
        }
        return list;
    }
//masuda$
}

