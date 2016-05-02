package sv.ues.fia.cargaacademicaeisi;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ControlDB {
	
	/* Mario */
	private static final String[] camposDepto = new String[] {
			"IDDEPARTAMENTO", "NOM_DEPTO" };
	private static final String[] camposMat = new String[] { "CODIGOMATERIA",
			"NOM_MATERIA" };
	private static final String[] camposAreaMat = new String[] { "IDAREAMAT",
			"IDDEPARTAMENTO", "CODIGOMATERIA" };
	private static final String[] camposDetGpoAsig = new String[] {
			"IDDETALLECURSO", "CODIGOMATERIA", "IDMODALIDAD", "IDLOCAL" };

	private static final String[] camposLocal = new String[] { "IDLOCAL",
			"CAPACIDAD" };
	private static final String[] camposModalidadAA = new String[] {
			"IDMODALIDAD", "NOM_MODALIDAD", "DESCUENTO_HORAS" };

	private static final String[] camposModCurso = new String[] {
			"IDMODALIDAD", "NOM_MODALIDAD", "DESCUENTO_HORAS" };

	private static final String[] camposActAcademica = new String[] {
			"IDACTACAD", "IDMODALIDAD", "NOM_ACT_ACAD", "CARGO" };

	private static final String[] camposCargaAcademica = new String[] {
			"IDDOCENTE", "ANIO", "NUMERO" };

	private static final String[] camposCiclo = new String[] { "ANIO",
			"NUMERO", "FECHAINI", "FECHAFIN" };

	
	/* Yo */
	private static final String[] camposContrato = new String[] { "IDCONTRATO",
			"TIPO", "HORAS" };
	private static final String[] camposDocente = new String[] { "IDDOCENTE",
		"IDCONTRATO", "NOMBRE", "APELLIDO", "GRADO_ACAD", "CORREO", "TELEFONO", "HORAS_ASIG" };
	private static final String[] CodContDoc = new String[] { "IDCONTRATO"};
	private static final String[] camposDocDepto = new String[] { "IDDEPARTAMENTO",
		"IDDOCENTE" };
	private static final String[] camposMatImpartir = new String[] {"IDDOCENTE",
	"IDAREAMAT"};
	/* Fin YO */
	
	private static final String[]camposCARGO = new String [] {"IDCARGO","NOM_CARGO"};
	private static final String[]camposDOCENTE_CARGO = new String [] {"IDDOCCAR","IDDOCENTE","IDPERIODO","IDCARGO"};
	private static final String[]camposPERIODO = new String [] {"IDPERIODO","FECHA_INI","FECHA_FIN"};
	
	
	private final Context context;
	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;

	public ControlDB(Context ctx) {
		this.context = ctx;
		DBHelper = new DatabaseHelper(context);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		private static final String BASE_DATOS = "databasecargaacademica.s3db";
		private static final int VERSION = 1;

		public DatabaseHelper(Context context) {
			super(context, BASE_DATOS, null, VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				db.execSQL("CREATE TABLE ACTIVIDAD_ACADEMICA ( IDACTACAD VARCHAR(6) NOT NULL PRIMARY KEY,IDMODALIDAD VARCHAR(6), NOM_ACT_ACAD VARCHAR(30), CARGO VARCHAR(20), CONSTRAINT fk_activida_modalida FOREIGN KEY (IDMODALIDAD) REFERENCES MODALIDAD_ACT_ACAD (IDMODALIDAD) ON DELETE RESTRICT);");
				db.execSQL("CREATE TABLE AREA_MATERIA ( IDAREAMAT VARCHAR(6) NOT NULL PRIMARY KEY, IDDEPARTAMENTO VARCHAR(6), CODIGOMATERIA VARCHAR(6), CONSTRAINT fk_area_departam FOREIGN KEY (IDDEPARTAMENTO) REFERENCES DEPARTAMENTO (IDDEPARTAMENTO) ON DELETE RESTRICT, CONSTRAINT fk_area_materia FOREIGN KEY (CODIGOMATERIA) REFERENCES MATERIA (CODIGOMATERIA) ON DELETE RESTRICT);");
				db.execSQL("CREATE TABLE [CARGA_ACADEMICA] ( [IDDOCENTE] VARCHAR(8)  NOT NULL, [ANIO] VARCHAR(4)  NOT NULL, [NUMERO] VARCHAR(2) NOT NULL, PRIMARY KEY ([IDDOCENTE],[ANIO],[NUMERO]));");
				db.execSQL("CREATE TABLE CARGO ( IDCARGO VARCHAR(5) NOT NULL PRIMARY KEY, NOM_CARGO  VARCHAR(20));");
				db.execSQL("CREATE TABLE [CICLO] ( [ANIO] VARCHAR(4)  NOT NULL, [NUMERO] VARCHAR(2)  NOT NULL, [FECHAINI] DATE DEFAULT CURRENT_DATE NULL, [FECHAFIN] DATE  NULL, PRIMARY KEY ([ANIO],[NUMERO]));");
				db.execSQL("CREATE TABLE DEPARTAMENTO ( IDDEPARTAMENTO  VARCHAR(6)  NOT NULL PRIMARY KEY, NOM_DEPTO VARCHAR(20));");
				db.execSQL("CREATE TABLE [DETALLE_CARGA_ACT_ACAD] ( [IDDOCENTE] VARCHAR(8) NULL, [ANIO] VARCHAR(4) NULL, [NUMERO] VARCHAR(2) NULL, [IDACTACAD] VARCHAR(6) NULL);");
				db.execSQL("CREATE TABLE [DETALLE_CARGA_MAT] ( [IDDOCENTE] VARCHAR(8) NULL, [ANIO] VARCHAR(4) NULL, [NUMERO] VARCHAR(2) NULL, [IDDETALLECURSO] VARCHAR(6) NULL);");
				db.execSQL("CREATE TABLE DETALLE_GRUPO_ASIGNADO ( IDDETALLECURSO  VARCHAR(6)  NOT NULL PRIMARY KEY, CODIGOMATERIA VARCHAR(6), IDMODALIDAD VARCHAR(6), IDLOCAL VARCHAR(6), constraint fk_detalle_materia FOREIGN KEY (CODIGOMATERIA) REFERENCES MATERIA (CODIGOMATERIA) ON DELETE RESTRICT, constraint fk_detalle_modalidad FOREIGN KEY (IDMODALIDAD) REFERENCES MODALIDAD_CURSO (IDMODALIDAD) ON DELETE RESTRICT, constraint fk_detalle_local FOREIGN KEY (IDLOCAL) REFERENCES LOCALES (IDLOCAL) ON DELETE RESTRICT);");
				db.execSQL("CREATE TABLE [DOCENTE] ( [IDDOCENTE] VARCHAR(8) PRIMARY KEY NOT NULL, [IDCONTRATO] VARCHAR(5) NULL, [NOMBRE] VARCHAR(50) NULL, [APELLIDO] VARCHAR(50) NULL, [GRADO_ACAD] VARCHAR(25) NULL, [CORREO] VARCHAR(20) NULL, [TELEFONO] VARCHAR(15) NULL, [HORAS_ASIG] INTEGER NULL);");
				db.execSQL("CREATE TABLE DOCENTE_CARGO ( IDDOCCAR VARCHAR(6) NOT NULL PRIMARY KEY, IDDOCENTE VARCHAR(8), IDPERIODO VARCHAR(6), IDCARGO VARCHAR(5), CONSTRAINT fk_docente_periodo FOREIGN KEY (IDPERIODO) REFERENCES PERIODO (IDPERIODO) ON DELETE RESTRICT, CONSTRAINT fk_docente_cargo FOREIGN KEY (IDCARGO) REFERENCES CARGO (IDCARGO) ON DELETE RESTRICT, CONSTRAINT fk_docente_docente FOREIGN KEY (IDDOCENTE) REFERENCES DOCENTE (IDDOCENTE) ON DELETE RESTRICT);");
				db.execSQL("CREATE TABLE DOCENTE_DPTO ( IDDEPARTAMENTO VARCHAR(6) NOT NULL, IDDOCENTE VARCHAR(8) NOT NULL, PRIMARY KEY (IDDEPARTAMENTO, IDDOCENTE), CONSTRAINT fk_docent_departam FOREIGN KEY (IDDEPARTAMENTO) REFERENCES DEPARTAMENTO (IDDEPARTAMENTO) ON DELETE RESTRICT, CONSTRAINT fk_docente_pertenece FOREIGN KEY (IDDOCENTE) REFERENCES DOCENTE (IDDOCENTE) ON DELETE RESTRICT);");
				db.execSQL("CREATE TABLE [LOCALES] ( [IDLOCAL] VARCHAR(6)  PRIMARY KEY NOT NULL,[CAPACIDAD] INTEGER  NULL);");
				db.execSQL("CREATE TABLE [MATERIA] ( [CODIGOMATERIA] VARCHAR(6)  PRIMARY KEY NOT NULL,[NOM_MATERIA] VARCHAR(20)  NULL);");
				db.execSQL("CREATE TABLE MAT_AREA_PUEDE_IMPARTIR ( IDDOCENTE  VARCHAR(8)  NOT NULL, IDAREAMAT  VARCHAR(6)  NOT NULL, PRIMARY KEY (IDDOCENTE, IDAREAMAT), CONSTRAINT fk_mat_area FOREIGN KEY (IDAREAMAT) REFERENCES AREA_MATERIA (IDAREAMAT) ON DELETE RESTRICT, CONSTRAINT fk_mat_docente FOREIGN KEY (IDDOCENTE) REFERENCES DOCENTE (IDDOCENTE) ON DELETE RESTRICT);");
				db.execSQL("CREATE TABLE [MODALIDAD_ACT_ACAD] ( [IDMODALIDAD] VARCHAR(6) PRIMARY KEY NOT NULL, [NOM_MODALIDAD] VARCHAR(25) NULL, [DESCUENTO_HORAS] INTEGER  NULL);");
				db.execSQL("CREATE TABLE MODALIDAD_CURSO ( IDMODALIDAD VARCHAR(6) NOT NULL PRIMARY KEY, NOM_MODALIDAD VARCHAR(20), DESCUENTO_HORAS  INTEGER);");
				db.execSQL("CREATE TABLE PERIODO ( IDPERIODO VARCHAR(6) NOT NULL PRIMARY KEY, FECHA_INI DATE, FECHA_FIN DATE);");
				db.execSQL("CREATE TABLE TIPO_CONTRATO ( IDCONTRATO VARCHAR(5) NOT NULL PRIMARY KEY, TIPO VARCHAR(25), HORAS INTEGER);");
				db.execSQL("CREATE TRIGGER fk_activida_modalida BEFORE INSERT ON ACTIVIDAD_ACADEMICA FOR EACH ROW BEGIN SELECT CASE WHEN ((SELECT IDMODALIDAD FROM MODALIDAD_ACT_ACAD WHERE IDMODALIDAD = NEW.IDMODALIDAD) IS NULL) THEN RAISE(ABORT, 'No existe esta Modalidad') END; END;");
				db.execSQL("CREATE TRIGGER fk_area_departam BEFORE INSERT ON AREA_MATERIA FOR EACH ROW BEGIN SELECT CASE WHEN ((SELECT IDDEPARTAMENTO FROM DEPARTAMENTO WHERE IDDEPARTAMENTO = NEW.IDDEPARTAMENTO) IS NULL) THEN RAISE(ABORT, 'No existe el Departamento') END; END;");
				db.execSQL("CREATE TRIGGER fk_area_materia BEFORE INSERT ON AREA_MATERIA FOR EACH ROW BEGIN SELECT CASE WHEN ((SELECT CODIGOMATERIA FROM MATERIA WHERE CODIGOMATERIA = NEW.CODIGOMATERIA) IS NULL) THEN RAISE(ABORT, 'No existe la Materia') END; END;");
				db.execSQL("CREATE TRIGGER [fk_carga_ciclo] BEFORE INSERT ON [CARGA_ACADEMICA] FOR EACH ROW BEGIN SELECT CASE WHEN ((SELECT ANIO FROM CICLO WHERE (ANIO = NEW.ANIO AND NUMERO = NEW.NUMERO)) IS NULL) THEN RAISE(ABORT, 'No existe el Ciclo') END; END;");
				db.execSQL("CREATE TRIGGER [fk_carga_docente] BEFORE INSERT ON [CARGA_ACADEMICA] FOR EACH ROW BEGIN SELECT CASE WHEN ((SELECT IDDOCENTE FROM DOCENTE WHERE IDDOCENTE = NEW.IDDOCENTE) IS NULL) THEN RAISE(ABORT, 'No existe el Docente') END; END;");
				db.execSQL("CREATE TRIGGER [fk_detalle_actividad] BEFORE INSERT ON [DETALLE_CARGA_ACT_ACAD] FOR EACH ROW BEGIN SELECT CASE WHEN ((SELECT IDACTACAD FROM ACTIVIDAD_ACADEMICA WHERE IDACTACAD = NEW.IDACTACAD) IS NULL) THEN RAISE(ABORT, 'No existe esta Actividad Academica') END; END;");
				db.execSQL("CREATE TRIGGER [fk_detalle_carga_acad] BEFORE INSERT ON [DETALLE_CARGA_ACT_ACAD] FOR EACH ROW BEGIN SELECT CASE WHEN ((SELECT IDDOCENTE FROM CARGA_ACADEMICA WHERE IDDOCENTE = NEW.IDDOCENTE AND ANIO = NEW.ANIO AND NUMERO = NEW.NUMERO) IS NULL) THEN RAISE(ABORT, 'No existe esta informacion de Carga Academica') END;END;");
				db.execSQL("CREATE TRIGGER [updateHorasActAcadMas] AFTER INSERT ON [DETALLE_CARGA_ACT_ACAD] FOR EACH ROW BEGIN UPDATE DOCENTE SET HORAS_ASIG = HORAS_ASIG + (SELECT DESCUENTO_HORAS FROM MODALIDAD_ACT_ACAD WHERE IDMODALIDAD = (SELECT IDMODALIDAD FROM ACTIVIDAD_ACADEMICA WHERE IDACTACAD = NEW.IDACTACAD)) WHERE DOCENTE.IDDOCENTE = NEW.IDDOCENTE; END;");
				db.execSQL("CREATE TRIGGER [updateHorasActAcadMenos] AFTER INSERT ON [DETALLE_CARGA_ACT_ACAD] FOR EACH ROW BEGIN UPDATE DOCENTE SET HORAS_ASIG = HORAS_ASIG - (SELECT DESCUENTO_HORAS FROM MODALIDAD_ACT_ACAD WHERE IDMODALIDAD = (SELECT IDMODALIDAD FROM ACTIVIDAD_ACADEMICA WHERE IDACTACAD = NEW.IDACTACAD)) WHERE DOCENTE.IDDOCENTE = NEW.IDDOCENTE;END;");
				//db.execSQL("CREATE TRIGGER StopCargaActAcad BEFORE INSERT ON DETALLE_CARGA_ACT_ACAD FOR EACH ROW BEGIN SELECT CASE WHEN ((SELECT HORAS_ASIG FROM DOCENTE WHERE IDDOCENTE = NEW.IDDOCENTE)>=(SELECT HORAS FROM TIPO_CONTRATO WHERE IDCONTRATO = (SELECT IDCONTRATO FROM DOCENTE WHERE IDDOCENTE = NEW.IDDOCENTE))) THEN RAISE(ABORT, 'Ya NO se le puede asiganar mas carga') END;END;");
				db.execSQL("CREATE TRIGGER [fk_detalle_curso] BEFORE INSERT ON [DETALLE_CARGA_MAT] FOR EACH ROW BEGIN SELECT CASE WHEN ((SELECT IDDETALLECURSO FROM DETALLE_GRUPO_ASIGNADO WHERE IDDETALLECURSO = NEW.IDDETALLECURSO) IS NULL) THEN RAISE(ABORT, 'No existe esta informacion de este Curso') END;END;");
				db.execSQL("CREATE TRIGGER [fk_detalle_carga_mat] BEFORE INSERT ON [DETALLE_CARGA_MAT] FOR EACH ROW BEGIN SELECT CASE WHEN ((SELECT IDDOCENTE FROM CARGA_ACADEMICA WHERE IDDOCENTE = NEW.IDDOCENTE AND ANIO = NEW.ANIO AND NUMERO = NEW.NUMERO) IS NULL) THEN RAISE(ABORT, 'No existe esta informacion de Carga Academica') END; END;");
				db.execSQL("CREATE TRIGGER [updateHorasMatMas] AFTER INSERT ON [DETALLE_CARGA_MAT] FOR EACH ROW BEGIN UPDATE DOCENTE SET HORAS_ASIG = HORAS_ASIG + (SELECT DESCUENTO_HORAS FROM MODALIDAD_CURSO WHERE IDMODALIDAD = (SELECT IDMODALIDAD FROM DETALLE_GRUPO_ASIGNADO WHERE IDDETALLECURSO = NEW.IDDETALLECURSO)) WHERE DOCENTE.IDDOCENTE = NEW.IDDOCENTE; END;");
				db.execSQL("CREATE TRIGGER [updateHorasMatMenos] AFTER DELETE ON [DETALLE_CARGA_MAT] FOR EACH ROW BEGIN UPDATE DOCENTE SET HORAS_ASIG = HORAS_ASIG - (SELECT DESCUENTO_HORAS FROM MODALIDAD_CURSO WHERE IDMODALIDAD = (SELECT IDMODALIDAD FROM DETALLE_GRUPO_ASIGNADO WHERE IDDETALLECURSO = OLD.IDDETALLECURSO)) WHERE DOCENTE.IDDOCENTE = OLD.IDDOCENTE; END;");
				//db.execSQL("CREATE TRIGGER [StopCargaMat] BEFORE INSERT ON [DETALLE_CARGA_MAT] FOR EACH ROW BEGIN SELECT CASE WHEN ((SELECT HORAS_ASIG FROM DOCENTE WHERE IDDOCENTE = NEW.IDDOCENTE)>=(SELECT HORAS FROM TIPO_CONTRATO WHERE IDCONTRATO = (SELECT IDCONTRATO FROM DOCENTE WHERE IDDOCENTE = NEW.IDDOCENTE))) THEN RAISE(ABORT, 'Ya NO se le puede asiganar mas carga') END;END;");
				db.execSQL("CREATE TRIGGER fk_detalle_materia BEFORE INSERT ON DETALLE_GRUPO_ASIGNADO FOR EACH ROW BEGIN SELECT CASE WHEN ((SELECT CODIGOMATERIA FROM MATERIA WHERE CODIGOMATERIA = NEW.CODIGOMATERIA) IS NULL) THEN RAISE(ABORT, 'No existe esta Materia') END;END;");
				db.execSQL("CREATE TRIGGER fk_detalle_modalidad BEFORE INSERT ON DETALLE_GRUPO_ASIGNADO FOR EACH ROW BEGIN SELECT CASE WHEN ((SELECT IDMODALIDAD FROM MODALIDAD_CURSO WHERE IDMODALIDAD = NEW.IDMODALIDAD) IS NULL) THEN RAISE(ABORT, 'No existe esta Modalidad') END; END;");
				db.execSQL("CREATE TRIGGER fk_detalle_local BEFORE INSERT ON DETALLE_GRUPO_ASIGNADO FOR EACH ROW BEGIN SELECT CASE WHEN ((SELECT IDLOCAL FROM LOCALES WHERE IDLOCAL = NEW.IDLOCAL) IS NULL) THEN RAISE(ABORT, 'No existe este Local') END; END;");
				db.execSQL("CREATE TRIGGER [fk_docente_contrato] BEFORE INSERT ON [DOCENTE] FOR EACH ROW BEGIN SELECT CASE WHEN ((SELECT IDCONTRATO FROM TIPO_CONTRATO WHERE IDCONTRATO = NEW.IDCONTRATO) IS NULL) THEN RAISE(ABORT, 'No existe este Tipo de Contrato') END;END;");
				db.execSQL("CREATE TRIGGER fk_docente_periodo BEFORE INSERT ON DOCENTE_CARGO FOR EACH ROW BEGIN SELECT CASE WHEN ((SELECT IDPERIODO FROM PERIODO WHERE IDPERIODO = NEW.IDPERIODO) IS NULL) THEN RAISE(ABORT, 'No existe este Periodo') END; END;");
				db.execSQL("CREATE TRIGGER fk_docente_cargo BEFORE INSERT ON DOCENTE_CARGO FOR EACH ROW BEGIN SELECT CASE WHEN ((SELECT IDCARGO FROM CARGO WHERE IDCARGO = NEW.IDCARGO) IS NULL) THEN RAISE(ABORT, 'No existe este Cargo') END; END;");
				db.execSQL("CREATE TRIGGER fk_docente_docente BEFORE INSERT ON DOCENTE_CARGO FOR EACH ROW BEGIN SELECT CASE WHEN ((SELECT IDDOCENTE FROM DOCENTE WHERE IDDOCENTE = NEW.IDDOCENTE) IS NULL) THEN RAISE(ABORT, 'No existe este Docente') END;END;");
				db.execSQL("CREATE TRIGGER fk_docente_pertenece BEFORE INSERT ON DOCENTE_DPTO FOR EACH ROW BEGIN SELECT CASE WHEN ((SELECT IDDOCENTE FROM DOCENTE WHERE IDDOCENTE = NEW.IDDOCENTE) IS NULL) THEN RAISE(ABORT, 'No existe este Docente') END;END;");
				db.execSQL("CREATE TRIGGER [fk_docente_departamento] BEFORE INSERT ON [DOCENTE_DPTO] FOR EACH ROW BEGIN SELECT CASE WHEN ((SELECT IDDEPARTAMENTO FROM DEPARTAMENTO WHERE IDDEPARTAMENTO = NEW.IDDEPARTAMENTO) IS NULL) THEN RAISE(ABORT, 'No existe este Departamento') END;END;");
				db.execSQL("CREATE TRIGGER fk_mat_area BEFORE INSERT ON MAT_AREA_PUEDE_IMPARTIR FOR EACH ROW BEGIN SELECT CASE WHEN ((SELECT IDAREAMAT FROM AREA_MATERIA WHERE IDAREAMAT = NEW.IDAREAMAT) IS NULL) THEN RAISE(ABORT, 'No existe esta Materia') END; END;");
				db.execSQL("CREATE TRIGGER fk_mat_docente BEFORE INSERT ON MAT_AREA_PUEDE_IMPARTIR FOR EACH ROW BEGIN SELECT CASE WHEN ((SELECT IDDOCENTE FROM DOCENTE WHERE IDDOCENTE = NEW.IDDOCENTE) IS NULL) THEN RAISE(ABORT, 'No existe este Docente') END; END;");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		}
	}

	public void abrir() throws SQLException {
		db = DBHelper.getWritableDatabase();
		return;
	}

	public void cerrar() {
		DBHelper.close();
	}

	/** TODO EL CODIGO DE CONTROL DE DCONTROLD DE BD ASIGNACION alexis */

	// Conseguir todas las etiquetas lista retornos de etiquetas!! IMPORTANTE
	public List<String> getAllLabels(String selectQuery, int posicion) {
		List<String> labels = new ArrayList<String>();

		// Select All Query
		// String selectQuery = "SELECT  * FROM PAIS order by nom_pais" ;

		db = DBHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				labels.add(cursor.getString(posicion));
			} while (cursor.moveToNext());
		}

		// closing connection
		cursor.close();
		db.close();

		// returning lables
		return labels;
	}

	// TABLA CICLO**************************************************
	public String insertarCiclo(Ciclo ciclo) {
		String regInsertados = "Registro de Ciclo Insertado Nş= ";
		long contador = 0;
		ContentValues ciclo1 = new ContentValues();
		ciclo1.put("ANIO", ciclo.getAnio());
		ciclo1.put("NUMERO", ciclo.getNumero());
		ciclo1.put("FECHAINI", ciclo.getFechaini());
		ciclo1.put("FECHAFIN", ciclo.getFechafin());

		contador = db.insert("CICLO", null, ciclo1);

		if (contador == -1 || contador == 0) {
			regInsertados = "Error al Insertar el registro Ciclo, Ciclo Duplicado. Verificar Inserción";
		} else {
			regInsertados = regInsertados + contador;
		}
		return regInsertados;
	}

	public Ciclo consultarCiclo(String anio, String numciclo) {
		String[] id = { anio, numciclo };
		Cursor cursor = db.query("CICLO", camposCiclo,
				"ANIO = ? AND NUMERO = ?", id, null, null, null);
		if (cursor.moveToFirst()) {
			Ciclo ciclo = new Ciclo();
			ciclo.setAnio(cursor.getString(0));
			ciclo.setNumero(cursor.getString(1));
			ciclo.setFechaini(cursor.getString(2));
			ciclo.setFechafin(cursor.getString(3));
			return ciclo;
		} else {
			return null;
		}
	}

	public String eliminar(Ciclo ciclo) {
		String regAfectados = "Ciclo Eliminado Correctamente, afectados= ";
		String where = "ANIO='" + ciclo.getAnio() + "'" + " AND NUMERO='"
				+ ciclo.getNumero() + "'";
		int contador = 0;
		if (verificarIntegridad(ciclo, 1)) {
			// contador += db.delete("ciclo",where, null);
			regAfectados = "No es posible eliminar, Existe carga academica asignada a este ciclo.";
		} else {
			contador += db.delete("CICLO", where, null);
			regAfectados += contador;
		}

		return regAfectados;
	}

	public String actualizar(Ciclo ciclo) {

		String[] id = { ciclo.getAnio(), ciclo.getNumero() };
		ContentValues cv = new ContentValues();
		cv.put("FECHAINI", ciclo.getFechaini());
		cv.put("FECHAFIN", ciclo.getFechafin());
		db.update("CICLO", cv, "ANIO = ? AND NUMERO = ?", id);
		return "Registro Actualizado Correctamente";
	}

	// TABLA CARGA ACADEMICA********************************************
	public String insertar(Carga_Academica carga) {
		String regInsertados = "Registro Carga Academica Insertado Nş= ";
		long contador = 0;

		if (verificarIntegridad(carga, 2)) {
			ContentValues carga_acad = new ContentValues();
			carga_acad.put("IDDOCENTE", carga.getIddocente());
			carga_acad.put("ANIO", carga.getAnio());
			carga_acad.put("NUMERO", carga.getNumero());
			contador = db.insert("CARGA_ACADEMICA", null, carga_acad);
		}

		if (contador == -1 || contador == 0) {
			regInsertados = "Error al Insertar el Carga Academica, Registro Duplicado. Verificar inserción";
		} else {
			regInsertados = regInsertados + contador;
		}

		return regInsertados;
	}

	public String eliminar(Carga_Academica carga) {
		// verificando si tiene REGISTROS hijos
		String regAfectados = "Registro Carga Academica Eliminados= ";
		String where = "IDDOCENTE ='" + carga.getIddocente() + "' AND ANIO = '"
				+ carga.getAnio() + "' AND NUMERO='" + carga.getNumero() + "'";
		int contador = 0;
		if (verificarIntegridad(carga, 3)) {
			// contador += db.delete("ciclo",where, null);
			regAfectados = "No es posible eliminar, Existe Carga de Materias o Actividades Academicas Asignadas a este Docente.";
		} else {
			contador += db.delete("CARGA_ACADEMICA", where, null);
			regAfectados += contador;
		}

		return regAfectados;
	}

	// TABLA DETALLE_CARGA_MAT**************************************************
	// VERIFICAR INTEGRIDAD?????????????????????????????????????????????????
	public String insertar(Detalle_Carga_Mat cargamat) {
		String regInsertados = "Registro Insertado de Carga de Materias  Nş= ";
		long contador = 0;
		ContentValues cargamaterias = new ContentValues();
		cargamaterias.put("IDDOCENTE", cargamat.getIddocente());
		cargamaterias.put("ANIO", cargamat.getAnio());
		cargamaterias.put("NUMERO", cargamat.getNumero());
		cargamaterias.put("IDDETALLECURSO", cargamat.getIddetallecurso());

		if (verificarIntegridad(cargamat, 4)) {
			// contador += db.delete("ciclo",where, null);
			regInsertados = "Error al Insertar Carga de Materias, Registro Duplicado. Verificar inserción";
		} else {
			contador = db.insert("DETALLE_CARGA_MAT", null, cargamaterias);
			regInsertados = regInsertados + contador;
		}

		/*
		 * if (contador == -1 || contador == 0) { regInsertados =
		 * "Error al Insertar el registro Ciclo, Ciclo Duplicado. Verificar Inserción"
		 * ; } else { regInsertados = regInsertados + contador; }
		 */
		return regInsertados;
	}

	public String eliminar(Detalle_Carga_Mat cargamat) {
		// verificando si tiene REGISTROS hijos
		String regAfectados = "Registro Carga Materia Eliminados= ";
		String where = "IDDOCENTE ='" + cargamat.getIddocente()
				+ "' AND ANIO = '" + cargamat.getAnio() + "' AND NUMERO='"
				+ cargamat.getNumero() + "' AND IDDETALLECURSO='"
				+ cargamat.getIddetallecurso() + "'";
		int contador = 0;
		contador += db.delete("DETALLE_CARGA_MAT", where, null);
		regAfectados += contador;
		return regAfectados;
	}

	// TABLA DETALLE_ACT_ACAD**************************************************

	// VERIFICAR INTEGRIDAD?????????????????????????????????????????????????
	public String insertar(Detalle_Carga_ActAcad cargamat) {
		String regInsertados = "Registro Insertado de Detalle Carga de Academica  Nş= ";
		long contador = 0;
		ContentValues cargaactividad = new ContentValues();
		cargaactividad.put("IDDOCENTE", cargamat.getIddocente());
		cargaactividad.put("ANIO", cargamat.getAnio());
		cargaactividad.put("NUMERO", cargamat.getNumero());
		cargaactividad.put("IDACTACAD", cargamat.getIdactacad());

		if (verificarIntegridad(cargamat, 5)) {
			regInsertados = "Error al Insertar el Detalle de Carga Academica, Registro Duplicado. Verificar inserción";
		} else {
			contador = db
					.insert("DETALLE_CARGA_ACT_ACAD", null, cargaactividad);
			regInsertados = regInsertados + contador;
		}
		return regInsertados;
	}

	public String eliminar(Detalle_Carga_ActAcad cargaactiv) {
		// verificando si tiene REGISTROS hijos
		String regAfectados = "Registro Carga Materia Eliminados= ";
		String where = "IDDOCENTE ='" + cargaactiv.getIddocente()
				+ "' AND ANIO = '" + cargaactiv.getAnio() + "' AND NUMERO='"
				+ cargaactiv.getNumero() + "' AND IDACTACAD='"
				+ cargaactiv.getIdactacad() + "'";
		int contador = 0;
		contador += db.delete("DETALLE_CARGA_ACT_ACAD", where, null);
		regAfectados += contador;
		return regAfectados;
	}

	/** METODOS MARIO */
	public String insertar(Departamento departamento) {
		String regInsertados = "Registro insertado en la fila No.=";
		long contador = 0;
		ContentValues depto = new ContentValues();
		depto.put("IDDEPARTAMENTO", departamento.getIddepartamento());
		depto.put("NOM_DEPTO", departamento.getNom_depto());
		contador = db.insert("DEPARTAMENTO", null, depto);
		if (contador == -1 || contador == 0) {
			regInsertados = "Error, registro duplicado. Verificar Insercion";
		} else {
			regInsertados += contador;
		}
		return regInsertados;
	}

	public String insertar(Materia materia) {
		String regInsertados = "Registro insertado en la fila No.=";
		long contador = 0;
		ContentValues mat = new ContentValues();
		mat.put("CODIGOMATERIA", materia.getCodigomateria());
		mat.put("NOM_MATERIA", materia.getNom_materia());
		contador = db.insert("MATERIA", null, mat);
		if (contador == -1 || contador == 0) {
			regInsertados = "Error, registro duplicado. Verificar Insercion";
		} else {
			regInsertados += contador;
		}
		return regInsertados;
	}

	public String insertar(AreaMateria areaMateria) {
		String regInsertados = "Registro insertado en la fila No.=";
		long contador = 0;
		ContentValues areamat = new ContentValues();
		areamat.put("IDAREAMAT", areaMateria.getIdareamat());
		areamat.put("IDDEPARTAMENTO", areaMateria.getIddepartamento());
		areamat.put("CODIGOMATERIA", areaMateria.getCodigomateria());
		contador = db.insert("AREA_MATERIA", null, areamat);
		if (contador == -1 || contador == 0) {
			regInsertados = "Error, registro duplicado. Verificar Insercion";
		} else {
			regInsertados += contador;
		}
		return regInsertados;
	}

	public String insertar(DetalleGrupoAsignado grupoAsignado) {
		String regInsertados = "Registro insertado en la fila No.=";
		long contador = 0;
		ContentValues gpoasig = new ContentValues();
		gpoasig.put("IDDETALLECURSO", grupoAsignado.getIddetallecurso());
		gpoasig.put("CODIGOMATERIA", grupoAsignado.getCodigomateria());
		gpoasig.put("IDMODALIDAD", grupoAsignado.getIdmodalidad());
		gpoasig.put("IDLOCAL", grupoAsignado.getIdlocal());
		contador = db.insert("DETALLE_GRUPO_ASIGNADO", null, gpoasig);
		if (contador == -1 || contador == 0) {
			regInsertados = "Error. Verificar Insercion";
		} else {
			regInsertados += contador;
		}
		return regInsertados;
	}

	public List<String> getAllIdModCurso() {
		List<String> idMaterias = new ArrayList<String>();
		Cursor cursor = db
				.rawQuery(
						"select IDMODALIDAD from MODALIDAD_CURSO order by IDMODALIDAD;",
						null);
		if (cursor.moveToFirst()) {
			do {
				idMaterias.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return idMaterias;
	}

	public Departamento consultarDepto(String idepto) {
		String[] id = { idepto };
		Cursor cursor = db.query("DEPARTAMENTO", camposDepto,
				"IDDEPARTAMENTO = ?", id, null, null, null);
		if (cursor.moveToFirst()) {
			Departamento departamento = new Departamento();
			departamento.setIddepartamento(cursor.getString(0));
			departamento.setNom_depto(cursor.getString(1));
			return departamento;
		} else {
			return null;
		}
	}

	public Materia consultarMateria(String codmat) {
		String[] id = { codmat };
		Cursor cursor = db.query("MATERIA", camposMat, "CODIGOMATERIA = ?", id,
				null, null, null);
		if (cursor.moveToFirst()) {
			Materia materia = new Materia();
			materia.setCodigomateria(cursor.getString(0));
			materia.setNom_materia(cursor.getString(1));
			return materia;
		} else {
			return null;
		}
	}

	public AreaMateria consultarAreaMateria(String idaremat) {
		String[] id = { idaremat };

		Cursor cursor = db.query("AREA_MATERIA", camposAreaMat,
				"IDAREAMAT = ?", id, null, null, null);

		/*
		 * String sql =
		 * "select idareamat,(select nom_depto from departamento) departamento, "
		 * + "(select nom_materia from materia) materia " +
		 * "from area_materia where idareamat='" + idaremat + "';";
		 * 
		 * Cursor cursor = db.rawQuery(sql, null);
		 */

		if (cursor.moveToFirst()) {
			AreaMateria areaMateria = new AreaMateria();
			areaMateria.setIdareamat(cursor.getString(0));
			areaMateria.setIddepartamento(cursor.getString(1));
			areaMateria.setCodigomateria(cursor.getString(2));
			return areaMateria;
		} else {
			return null;
		}
	}

	public DetalleGrupoAsignado consultarDetGpoAsig(String idetcurso) {
		String[] id = { idetcurso };
		Cursor cursor = db.query("DETALLE_GRUPO_ASIGNADO", camposDetGpoAsig,
				"IDDETALLECURSO = ?", id, null, null, null);
		if (cursor.moveToFirst()) {
			DetalleGrupoAsignado grupoAsignado = new DetalleGrupoAsignado();
			grupoAsignado.setIddetallecurso(cursor.getString(0));
			grupoAsignado.setCodigomateria(cursor.getString(1));
			grupoAsignado.setIdmodalidad(cursor.getString(2));
			grupoAsignado.setIdlocal(cursor.getString(3));
			return grupoAsignado;
		} else {
			return null;
		}
	}

	public String consultarTablas() {
		String tablas = "Tablas:";
		String[] id = { "table" };
		Cursor cursor = db.query("sqlite_master", new String[] { "name" },
				"type = ?", id, null, null, null);
		if (cursor.moveToFirst()) {
			tablas += "\n" + cursor.getString(0);
		} else {
			tablas = "No hay tablas";
		}
		return tablas;
	}

	public List<String> getAllIdDeptos() {
		List<String> idDeptos = new ArrayList<String>();
		Cursor cursor = db
				.rawQuery(
						"select IDDEPARTAMENTO from DEPARTAMENTO order by IDDEPARTAMENTO;",
						null);
		if (cursor.moveToFirst()) {
			do {
				idDeptos.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return idDeptos;
	}

	public List<String> getAllIdMaterias() {
		List<String> idMaterias = new ArrayList<String>();
		Cursor cursor = db.rawQuery(
				"select codigomateria from materia order by codigomateria;",
				null);
		if (cursor.moveToFirst()) {
			do {
				idMaterias.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return idMaterias;
	}

	public List<String> getAllIdAreaMats() {
		List<String> idMaterias = new ArrayList<String>();
		Cursor cursor = db.rawQuery(
				"select IDAREAMAT from AREA_MATERIA order by IDAREAMAT;", null);
		if (cursor.moveToFirst()) {
			do {
				idMaterias.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return idMaterias;
	}

	public List<String> getAllIdDetGpoAsig() {
		List<String> idMaterias = new ArrayList<String>();
		Cursor cursor = db
				.rawQuery(
						"select IDDETALLECURSO from DETALLE_GRUPO_ASIGNADO order by IDDETALLECURSO;",
						null);
		if (cursor.moveToFirst()) {
			do {
				idMaterias.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return idMaterias;
	}

	public String actualizar(Departamento departamento) {
		String[] id = { departamento.getIddepartamento() };
		ContentValues values = new ContentValues();
		values.put("NOM_DEPTO", departamento.getNom_depto());
		db.update("DEPARTAMENTO", values, "IDDEPARTAMENTO = ?", id);
		return "Registro actualizado correctamente";
	}

	public String actualizar(Materia materia) {
		String[] id = { materia.getCodigomateria() };
		ContentValues values = new ContentValues();
		values.put("NOM_MATERIA", materia.getNom_materia());
		db.update("MATERIA", values, "CODIGOMATERIA = ?", id);
		return "Registro actualizado correctamente";
	}

	public String eliminar(Departamento departamento) {
		String regAfectados = "";
		int contador = 0;
		if (verificarIntegridad(departamento, 7)
				|| verificarIntegridad(departamento, 8)) {
			regAfectados += "Tiene registros hijos\nNo se puede borrar,";
			if (verificarIntegridad(departamento, 7))
				regAfectados += " DOCENTE_DPTO tiene registros.";
			if (verificarIntegridad(departamento, 8))
				regAfectados += " AREA_MATERIA tiene registros.";
			return regAfectados;
		}

		regAfectados = "No tiene registros hijos\nFilas afectadas=";
		/*
		 * contador += db.delete("DOCENTE_DPTO", "IDDEPARTAMENTO='" +
		 * departamento.getIddepartamento() + "'", null);
		 * 
		 * contador += db.delete("AREA_MATERIA", "IDDEPARTAMENTO='" +
		 * departamento.getIddepartamento() + "'", null);
		 */
		contador += db.delete("DEPARTAMENTO",
				"IDDEPARTAMENTO='" + departamento.getIddepartamento() + "'",
				null);
		regAfectados += contador;
		return regAfectados;
	}

	public String eliminar(Materia materia) {
		String regAfectados = "";
		int contador = 0;
		if (verificarIntegridad(materia, 9) || verificarIntegridad(materia, 10)) {
			regAfectados += "Tiene registros hijos\nNo se puede borrar,";
			if (verificarIntegridad(materia, 9))
				regAfectados += " DETALLE_GRUPO_ASIGNADO tiene registros.";
			if (verificarIntegridad(materia, 10))
				regAfectados += " AREA_MATERIA tiene registros.";
			return regAfectados;
		}
		regAfectados = "No tiene registros hijos\nFilas afectadas=";
		contador += db.delete("MATERIA",
				"CODIGOMATERIA='" + materia.getCodigomateria() + "'", null);
		regAfectados += contador;
		return regAfectados;
	}

	public String eliminar(AreaMateria areaMateria) {
		String regAfectados = "";
		int contador = 0;
		regAfectados = "No tiene registros hijos\nFilas afectadas=";
		contador += db.delete("AREA_MATERIA",
				"IDAREAMAT='" + areaMateria.getIdareamat() + "'", null);
		regAfectados += contador;
		return regAfectados;
	}

	public String eliminar(DetalleGrupoAsignado grupoAsignado) {
		String regAfectados = "";
		int contador = 0;
		if (verificarIntegridad(grupoAsignado, 11)) {
			regAfectados += "No se puede borrar\nTiene registros hijos en DETALLE_CARGA_MAT.";
			return regAfectados;
		}
		regAfectados = "No tiene registros hijos\nFilas afectadas=";
		contador += db.delete("DETALLE_GRUPO_ASIGNADO", "IDDETALLECURSO='"
				+ grupoAsignado.getIddetallecurso() + "'", null);
		regAfectados += contador;
		return regAfectados;
	}

	public String getAsociado(String tabla, String campo, String clave,
			String valor) {
		String sql = "select " + campo + " from " + tabla + " where " + clave
				+ "='" + valor + "';";
		String asocido = "no hay dato";
		Cursor cursor = db.rawQuery(sql, null);
		if (cursor.moveToFirst()) {
			asocido = cursor.getString(0);
		}

		return asocido;
	}

	/** METODOS EMERSON */
	
	public String InsertarContrato(TipoContrato tipocontrato) {
		String regInsertados = "Registro insertado en la fila No.= ";
		long contador = 0;
		ContentValues contrato = new ContentValues();
		contrato.put("IDCONTRATO", tipocontrato.getIdContrato());
		contrato.put("TIPO", tipocontrato.getTipo());
		contrato.put("HORAS", tipocontrato.getHoras());
		contador = db.insert("TIPO_CONTRATO", null, contrato);
		if (contador == -1 || contador == 0) {
			regInsertados = "Error. Verificar Insercion";
		} else {
			regInsertados += contador;
		}

		return regInsertados;
	}

	public TipoContrato ConsultarContrato(String idcontrato) {
		String[] id = { idcontrato };
		Cursor cursor = db.query("TIPO_CONTRATO", camposContrato,
				"IDCONTRATO = ?", id, null, null, null);
		if (cursor.moveToFirst()) {
			TipoContrato contrato = new TipoContrato();
			contrato.setIdContrato(cursor.getString(0));
			contrato.setTipo(cursor.getString(1));
			contrato.setHoras(cursor.getInt(2));
			return contrato;
		} else {
			return null;
		}
	}
	
	public String ActualizarContrato(TipoContrato tipocont) {
		String[] id = { tipocont.getIdContrato() };
		ContentValues values = new ContentValues();
		values.put("TIPO", tipocont.getTipo());
		values.put("HORAS", tipocont.getHoras());
		db.update("TIPO_CONTRATO", values, "IDCONTRATO = ?", id);
		return "Registro actualizado correctamente";
	}

	public String EliminarContrato(TipoContrato tipocont) {
		String regAfectados = "";
		int contador = 0;
		if (verificarIntegridad(tipocont, 13)) {
			regAfectados += "Tiene registros hijos\nNo se puede borrar,";
			regAfectados += " DOCENTE tiene registros.";
			return regAfectados;
		}
		regAfectados = "No tiene registros hijos\nFilas afectadas=";
		contador += db.delete("TIPO_CONTRATO",
				"IDCONTRATO='" + tipocont.getIdContrato() + "'", null);
		regAfectados += contador;
		return regAfectados;
	}
	
	public List<String> getAllIdContratos() {
		List<String> idContratos = new ArrayList<String>();
		Cursor cursor = db.rawQuery(
				"select IDCONTRATO from TIPO_CONTRATO order by IDCONTRATO;",
				null);
		if (cursor.moveToFirst()) {
			do {
				idContratos.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return idContratos;
	}
	
	public List<String> getAllIdMatArea() {
		List<String> idContratos = new ArrayList<String>();
		Cursor cursor = db.rawQuery(
				"select IDAREAMAT from AREA_MATERIA order by IDAREAMAT;",
				null);
		if (cursor.moveToFirst()) {
			do {
				idContratos.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return idContratos;
	}
	
	public List<String> getAllIdDocMatImp() {
		List<String> idContratos = new ArrayList<String>();
		Cursor cursor = db.rawQuery(
				"select IDDOCENTE from MAT_AREA_PUEDE_IMPARTIR group by IDDOCENTE;",
				null);
		if (cursor.moveToFirst()) {
			do {
				idContratos.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return idContratos;
	}
	
	public List<String> getAllIdContratos2(String iddocente) {
		List<String> idContratos = new ArrayList<String>();
		String[] id = { iddocente };
		Cursor cursor = db.query("DOCENTE", CodContDoc,
				"IDDOCENTE = ?", id, null,  null, null);
		if (cursor.moveToFirst()) {
			do {
				idContratos.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return idContratos;
	}
	
	public List<String> getAllIdMatArea2(String iddocente) {
		List<String> idContratos = new ArrayList<String>();
		String[] id = { iddocente };
		Cursor cursor = db.query("MAT_AREA_PUEDE_IMPARTIR", camposMatImpartir,
				"IDDOCENTE = ?", id, null,  null, null);
		if (cursor.moveToFirst()) {
			do {
				idContratos.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return idContratos;
	}
	
	public List<String> getAllIdDocDocDepto() {
		List<String> idDoc = new ArrayList<String>();
		Cursor cursor = db.rawQuery(
				"select IDDOCENTE from DOCENTE_DPTO group by IDDOCENTE order by IDDOCENTE;",
				null);
		if (cursor.moveToFirst()) {
			do {
				idDoc.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return idDoc;
	}
	
	public List<String> getAllIdDeptoDocDepto(String iddocente) {
		List<String> idContratos = new ArrayList<String>();
		String[] id = { iddocente };
		Cursor cursor = db.query("DOCENTE_DPTO", camposDocDepto,
				"IDDOCENTE = ?", id, null,  null, null);
		if (cursor.moveToFirst()) {
			do {
				idContratos.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return idContratos;
	}
	
	public List<String> getAllIdAreaMatImp4(String iddocente) {
		List<String> idContratos = new ArrayList<String>();
		String[] id = { iddocente };
		Cursor cursor = db.query("MAT_AREA_PUEDE_IMPARTIR", camposMatImpartir,
				 "IDDOCENTE = ?", id, null,  null, null);
		if (cursor.moveToFirst()) {
			do {
				idContratos.add(cursor.getString(1));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return idContratos;
	}
	
	public String InsertarDocDepto(DocenteDepto docdepto) {
		String regInsertados = "Registro insertado en la fila No.= ";
		long contador = 0;
		ContentValues relacion = new ContentValues();
		relacion.put("IDDEPARTAMENTO", docdepto.getIdDepartamento());
		relacion.put("IDDOCENTE", docdepto.getIdDocente());
		contador = db.insert("DOCENTE_DPTO", null, relacion);
		if (contador == -1 || contador == 0) {
			regInsertados = "Error. Verificar Insercion";
		} else {
			regInsertados += contador;
		}

		return regInsertados;
	}
	
	public String EliminarDocenteDepto(DocenteDepto docente) {
		String regAfectados = "";
		int contador = 0;
		/*if (verificarIntegridad(docente, 15)) {
			regAfectados += "Tiene registros hijos\nNo se puede borrar,";
			return regAfectados;
		}*/
		regAfectados = "No tiene registros hijos\nFilas afectadas=";
		contador += db.delete("DOCENTE_DPTO",
				"IDDEPARTAMENTO = '"+ docente.getIdDepartamento() +"' AND IDDOCENTE='" + docente.getIdDocente() + "'", null);
		regAfectados += contador;
		return regAfectados;
	}
	
	public String EliminarMateriaImpartir(MateriasImpartir docente) {
		String regAfectados = "";
		int contador = 0;
		/*if (verificarIntegridad(docente, 15)) {
			regAfectados += "Tiene registros hijos\nNo se puede borrar,";
			return regAfectados;
		}*/
		regAfectados = "No tiene registros hijos\nFilas afectadas=";
		contador += db.delete("MAT_AREA_PUEDE_IMPARTIR",
				"IDAREAMAT = '"+ docente.getIdAreaMat() +"' AND IDDOCENTE='" + docente.getIdDocente() + "'", null);
		regAfectados += contador;
		return regAfectados;
	}
	
	public String InsertarMatImpart(MateriasImpartir mateimpart) {
		String regInsertados = "Registro insertado en la fila No.= ";
		long contador = 0;
		ContentValues relacion = new ContentValues();
		relacion.put("IDDOCENTE", mateimpart.getIdDocente());
		relacion.put("IDAREAMAT", mateimpart.getIdAreaMat());
		contador = db.insert("MAT_AREA_PUEDE_IMPARTIR", null, relacion);
		if (contador == -1 || contador == 0) {
			regInsertados = "Error. Verificar Insercion";
		} else {
			regInsertados += contador;
		}

		return regInsertados;
	}

	public String InsertarDocentes(Docente docente) {
		String regInsertados = "Registro insertado en la fila No.= ";
		long contador = 0;
		ContentValues doc = new ContentValues();
		doc.put("IDDOCENTE", docente.getIdDocente());
		doc.put("IDCONTRATO", docente.getIdContrato());
		doc.put("NOMBRE", docente.getNombre());
		doc.put("APELLIDO", docente.getApellido());
		doc.put("GRADO_ACAD", docente.getGradoAcademico());
		doc.put("CORREO", docente.getCorreo());
		doc.put("TELEFONO", docente.getTelefono());
		doc.put("HORAS_ASIG", docente.getHorasAsignadas());
		contador = db.insert("DOCENTE", null, doc);
		if (contador == -1 || contador == 0) {
			regInsertados = "Error. Verificar Insercion";
		} else {
			regInsertados += contador;
		}

		return regInsertados;
	}

	public Docente ConsultarDocente(String iddocente) {
		String[] id = { iddocente };
		Cursor cursor = db.query("DOCENTE", camposDocente,
				"IDDOCENTE = ? ", id, null,  null, null);
		if (cursor.moveToFirst()) {
			Docente docente = new Docente();
			docente.setIdDocente(cursor.getString(0));
			docente.setIdContrato(cursor.getString(1));
			docente.setNombre(cursor.getString(2));
			docente.setApellido(cursor.getString(3));
			docente.setGradoAcademico(cursor.getString(4));
			docente.setCorreo(cursor.getString(5));
			docente.setTelefono(cursor.getString(6));
			docente.setHorasAsignadas(cursor.getInt(7));
			return docente;
		} else {
			return null;
		}
	}
	
	public Docente ConsultarDocente2(String iddocente) {
		String[] id = { iddocente };
		Cursor cursor = db.query("DOCENTE", camposDocente,
				"IDDOCENTE = ?", id, null,  null, null);
		if (cursor.moveToFirst()) {
			Docente docente = new Docente();
			docente.setIdDocente(cursor.getString(0));
			docente.setIdContrato(cursor.getString(1));
			docente.setNombre(cursor.getString(2));
			docente.setApellido(cursor.getString(3));
			
			return docente;
		} else {
			return null;
		}
	}
	
	public AreaMateria ConsultarAreaMat(String idareamat) {
		String[] id = { idareamat };
		Cursor cursor = db.query("AREA_MATERIA", camposAreaMat,
				"IDAREAMAT = ?", id, null,  null, null);
		if (cursor.moveToFirst()) {
			AreaMateria areamat = new AreaMateria();
			areamat.setCodigomateria(cursor.getString(0));
			areamat.setIdareamat(cursor.getString(1));
			areamat.setIddepartamento(cursor.getString(2));
			return areamat;
		} else {
			return null;
		}
	}
	
	public String EliminarDocente(Docente docente) {
		String regAfectados = "";
		int contador = 0;
		if (verificarIntegridad(docente, 14)) {
			regAfectados += "Tiene registros hijos\nNo se puede borrar,";
			return regAfectados;
		}
		regAfectados = "No tiene registros hijos\nFilas afectadas=";
		contador += db.delete("DOCENTE",
				"IDDOCENTE = '"+ docente.getIdDocente() +"' AND IDCONTRATO='" + docente.getIdContrato() + "'", null);
		regAfectados += contador;
		return regAfectados;
	}
	public String ActualizarDocente(Docente docente) {
		String[] id = { docente.getIdDocente(), docente.getIdContrato() };
		ContentValues values = new ContentValues();
		values.put("NOMBRE", docente.getNombre());
		values.put("APELLIDO", docente.getApellido());
		values.put("GRADO_ACAD", docente.getGradoAcademico());
		values.put("CORREO", docente.getCorreo());
		values.put("TELEFONO", docente.getTelefono());
		db.update("DOCENTE", values, "IDDOCENTE = ? AND IDCONTRATO = ?", id);
		return "Registro actualizado correctamente";
	}
	/*Fin YO*/

	/** METODOS AGUSTIN */
	public String insertar(Locales local) {
		String regInsertados = "Registro insertado en la fila No.=";
		long contador = 0;
		ContentValues loc = new ContentValues();
		loc.put("IDLOCAL", local.getIdlocal());
		loc.put("CAPACIDAD", local.getCapacidad());
		contador = db.insert("LOCALES", null, loc);
		if (contador == -1 || contador == 0) {
			regInsertados = "Error. Verificar Insercion";
		} else {
			regInsertados += contador;
		}
		return regInsertados;
	}

	public String insertar(Modalidad_Act_Acad modalidadAA) {
		String regInsertados = "Registro insertado en la fila No.=";
		long contador = 0;
		ContentValues modalidad = new ContentValues();
		modalidad.put("IDMODALIDAD", modalidadAA.getIdmodalidad());
		modalidad.put("NOM_MODALIDAD", modalidadAA.getNom_modalidad());
		modalidad.put("DESCUENTO_HORAS", modalidadAA.getDescuento_horas());
		contador = db.insert("MODALIDAD_ACT_ACAD", null, modalidad);
		if (contador == -1 || contador == 0) {
			regInsertados = "Error. Verificar Insercion";
		} else {
			regInsertados += contador;
		}
		return regInsertados;
	}

	public String insertar(Modalidad_Curso modalidadcurso) {
		String regInsertados = "Registro insertado en la fila No.=";
		long contador = 0;
		ContentValues modcurso = new ContentValues();
		modcurso.put("IDMODALIDAD", modalidadcurso.getIdmodalidadCurso());
		modcurso.put("NOM_MODALIDAD", modalidadcurso.getNom_modalidad());
		modcurso.put("DESCUENTO_HORAS", modalidadcurso.getDescuento_horas());
		contador = db.insert("MODALIDAD_CURSO", null, modcurso);
		if (contador == -1 || contador == 0) {
			regInsertados = "Error. Verificar Insercion";
		} else {
			regInsertados += contador;
		}
		return regInsertados;
	}

	public String insertar(Actividad_Academica ActAcademica) {
		String regInsertados = "Registro insertado en la fila No.=";
		long contador = 0;
		ContentValues ActiviAcademica = new ContentValues();
		ActiviAcademica.put("IDACTACAD", ActAcademica.getIdactacad());
		ActiviAcademica.put("IDMODALIDAD", ActAcademica.getIdmodalidad());
		ActiviAcademica.put("NOM_ACT_ACAD", ActAcademica.getNom_act_acad());
		ActiviAcademica.put(" CARGO", ActAcademica.getCargo());
		contador = db.insert("ACTIVIDAD_ACADEMICA", null, ActiviAcademica);
		if (contador == -1 || contador == 0) {
			regInsertados = "Error. Verificar Insercion";
		} else {
			regInsertados += contador;
		}
		return regInsertados;
		
	}

	public List<String> getAll_IdLocales() {
		List<String> idLocales = new ArrayList<String>();
		Cursor cursor = db.rawQuery(
				"select IDLOCAL from LOCALES order by IDLOCAL;", null);
		if (cursor.moveToFirst()) {
			do {
				idLocales.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return idLocales;
	}

	public Locales consultarLocal(String idLocal) {
		String[] id = { idLocal };
		Cursor cursor = db.query("LOCALES", camposLocal, "IDLOCAL = ?", id,
				null, null, null);
		if (cursor.moveToFirst()) {
			Locales local = new Locales();
			local.setIdlocal(cursor.getString(0));
			local.setCapacidad(cursor.getString(1));
			return local;
		} else {
			return null;
		}

	}

	public Modalidad_Curso consultarModCurso(String idModCurso) {
		String[] id = { idModCurso };
		Cursor cursor = db.query("MODALIDAD_CURSO", camposModCurso,
				"IDMODALIDAD = ?", id, null, null, null);
		if (cursor.moveToFirst()) {
			Modalidad_Curso ModalCurso = new Modalidad_Curso();
			ModalCurso.setIdmodalidadCurso(cursor.getString(0));
			ModalCurso.setNom_modalidad(cursor.getString(1));
			ModalCurso.setDescuento_horas(cursor.getInt(2));
			return ModalCurso;
		} else {
			return null;
		}

	}

	public Modalidad_Act_Acad consultarModActAcad(String idModActA) {
		String[] id = { idModActA };
		Cursor cursor = db.query("MODALIDAD_ACT_ACAD", camposModalidadAA,
				"IDMODALIDAD = ?", id, null, null, null);
		if (cursor.moveToFirst()) {
			Modalidad_Act_Acad ModalAA = new Modalidad_Act_Acad();
			ModalAA.setIdmodalidad(cursor.getString(0));
			ModalAA.setNom_modalidad(cursor.getString(1));
			ModalAA.setDescuento_horas(cursor.getInt(2));
			return ModalAA;
		} else {
			return null;
		}
	}

	public List<String> getAll_IdModAA() {
		List<String> idModAA = new ArrayList<String>();
		Cursor cursor = db
				.rawQuery(
						"select IDMODALIDAD from MODALIDAD_ACT_ACAD order by IDMODALIDAD;",
						null);
		if (cursor.moveToFirst()) {
			do {
				idModAA.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return idModAA;
	}

	public Actividad_Academica consultarActAcademica(String idActAcademica) {
		String[] id = { idActAcademica };
		Cursor cursor = db.query("ACTIVIDAD_ACADEMICA", camposActAcademica,
				"IDACTACAD = ?", id, null, null, null);
		if (cursor.moveToFirst()) {
			Actividad_Academica ActAcad = new Actividad_Academica();
			ActAcad.setIdactacad(cursor.getString(0));
			ActAcad.setIdmodalidad(cursor.getString(1));
			ActAcad.setNom_act_acad(cursor.getString(2));
			ActAcad.setCargo(cursor.getString(3));
			return ActAcad;
		} else {
			return null;
		}
	}

	public List<String> getAll_IdActA() {
		List<String> idActA = new ArrayList<String>();
		Cursor cursor = db
				.rawQuery(
						"select IDACTACAD from ACTIVIDAD_ACADEMICA order by IDACTACAD;",
						null);
		if (cursor.moveToFirst()) {
			do {
				idActA.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return idActA;
	}

	public String eliminarLocales(Locales local) {
		String regAfectados = "";
		int contador = 0;
		if (verificarIntegridad(local, 19)) {
			regAfectados += "Tiene registros hijos\nNo se puede borrar,";
			return regAfectados;
		}
		regAfectados = "No tiene registros hijos\nFilas afectadas=";

		contador += db.delete("LOCALES","IDLOCAL='" + local.getIdlocal() + "'", null);
		regAfectados += contador;
		return regAfectados;
	}
	
	public String eliminarModCurso(Modalidad_Curso ModCurso) {
		String regAfectados = "";
		int contador = 0;
		if (verificarIntegridad(ModCurso, 22)) {
			regAfectados += "Tiene registros hijos\nNo se puede borrar,";
			return regAfectados;
		}
		regAfectados = "No tiene registros hijos\nFilas afectadas=";

		contador += db.delete("MODALIDAD_CURSO","IDMODALIDAD='" + ModCurso.getIdmodalidadCurso() + "'", null);
		regAfectados += contador;
		return regAfectados;
	}
	
	public String eliminarModalActAcad(Modalidad_Act_Acad ModalAA) {
		String regAfectados = "";
		int contador = 0;
		if (verificarIntegridad(ModalAA, 21)) {
			regAfectados += "Tiene registros hijos\nNo se puede borrar,";
			return regAfectados;
		}
		regAfectados = "No tiene registros hijos\nFilas afectadas=";

		contador += db.delete("MODALIDAD_ACT_ACAD","IDMODALIDAD='" + ModalAA.getIdmodalidad() + "'", null);
		regAfectados += contador;
		return regAfectados;
	}
	
	public String eliminarActAcad(Actividad_Academica ActAcademica) {
		String regAfectados = "";
		int contador = 0;
		if (verificarIntegridad(ActAcademica, 20)) {
			regAfectados += "Tiene registros hijos\nNo se puede borrar,";
			return regAfectados;
		}
		regAfectados = "No tiene registros hijos\nFilas afectadas=";

		contador += db.delete("ACTIVIDAD_ACADEMICA","IDACTACAD='" + ActAcademica.getIdactacad() + "'", null);
		regAfectados += contador;
		return regAfectados;		
	}
	
	public String actualizar(Locales local) {
		String[] id = {local.getIdlocal() };
		ContentValues values = new ContentValues();
		values.put("CAPACIDAD", local.getCapacidad());
		db.update("LOCALES", values, "IDLOCAL = ?", id);
		return "Registro actualizado correctamente";
	}
	
	public String actualizar(Modalidad_Curso ModCurso) {
		String[] id = {ModCurso.getIdmodalidadCurso() };
		ContentValues values = new ContentValues();
		values.put("NOM_MODALIDAD", ModCurso.getNom_modalidad());
		values.put("DESCUENTO_HORAS", ModCurso.getDescuento_horas());
		db.update("MODALIDAD_CURSO", values, "IDMODALIDAD = ?", id);
		return "Registro actualizado correctamente";
	}
	
	public String actualizar(Modalidad_Act_Acad ModActAcad) {
		String[] id = {ModActAcad.getIdmodalidad() };
		ContentValues values = new ContentValues();
		values.put("NOM_MODALIDAD", ModActAcad.getNom_modalidad());
		values.put("DESCUENTO_HORAS", ModActAcad.getDescuento_horas());
		db.update("MODALIDAD_ACT_ACAD", values, "IDMODALIDAD = ?", id);
		return "Registro actualizado correctamente";
	}
	
	public String actualizar(Actividad_Academica ActAcad) {
		String[] id = {ActAcad.getIdactacad() };
		ContentValues values = new ContentValues();
		values.put("IDMODALIDAD", ActAcad.getIdmodalidad());
		values.put("NOM_ACT_ACAD", ActAcad.getNom_act_acad());
		values.put("CARGO", ActAcad.getCargo());		
		db.update("ACTIVIDAD_ACADEMICA", values, "IDACTACAD = ?", id);
		return "Registro actualizado correctamente";
	}
	

	/** METODOS SERGIO  ***********************************************/

	public String insertar(CARGO cargo){
		String regInsertados = "Registro insertado en la fila No.=";
		long contador = 0;
		ContentValues car = new ContentValues();
		car.put("IDCARGO", cargo.getIdCargo());
		car.put("NOM_CARGO", cargo.getNomCargo());
		contador = db.insert("CARGO", null, car);
		if (contador == -1 || contador == 0) {
			regInsertados = "Error, registro duplicado. Verificar Insercion";
		} else {
			regInsertados += contador;
		}
		return regInsertados;
	}

	public String actualizar(CARGO cargo) {
		if(verificarIntegridad(cargo, 28)){
		String[] id = { cargo.getIdCargo() };
		ContentValues values = new ContentValues();
		values.put("NOM_CARGO", cargo.getNomCargo());
		db.update("CARGO", values, "IDCARGO = ?", id);
		return "Registro actualizado correctamente";
		}else{
			return "Registro con IdCargo " + cargo.getIdCargo() + " no existe";
		}
	}
	public CARGO consultarCargo(String idCargo) {
		String[] id = { idCargo };
		Cursor cursor = db.query("CARGO", camposCARGO,"IDCARGO = ?", id, null, null, null);
		if (cursor.moveToFirst()) {
			CARGO cargo = new CARGO();
			cargo.setIdCargo(cursor.getString(0));
			cargo.setNomCargo(cursor.getString(1));
			return cargo;
		} else {
			return null;
		}
	}
	public String eliminar(CARGO cargo) {
		String regAfectados = "";
		int contador = 0;
		if (verificarIntegridad(cargo, 25)){
			regAfectados += "Tiene registros hijos\nNo se puede borrar,";
			if (verificarIntegridad(cargo, 25))
				regAfectados += " DOCENTE_CARGO tiene registros.";
			return regAfectados;
		}

		regAfectados = "No tiene registros hijos\nFilas afectadas=";
		
		contador += db.delete("CARGO","IDCARGO='" + cargo.getIdCargo() + "'",null);
		regAfectados += contador;
		return regAfectados;
	}
	public String insertar(DOCENTE_CARGO cargoAsignado){
		
		String regInsertados="Registro Insertado Nş= ";
		long contador=0;
	ContentValues doccargos = new ContentValues();
		 doccargos.put("IDDOCCAR", cargoAsignado.getIdDocCar());
		// doccargos.put("IDDOCENTE", cargoAsignado.getIdDocente());
		 doccargos.put("IDPERIODO", cargoAsignado.getIdPeriodo());
		 doccargos.put("IDCARGO", cargoAsignado.getIdCargo());
		contador=db.insert("DOCENTE_CARGO", null, doccargos);

		if(contador==-1 || contador==0)
		{
			regInsertados= "Error al Insertar el registro, Registro Duplicado. Verificar inserción"+cargoAsignado.getIdDocCar();
		}
		else {
				regInsertados=regInsertados+contador;
		}
		
		return regInsertados;

	}
	public String eliminar(DOCENTE_CARGO cargoAsignado) {
		String regAfectados = "";
		int contador = 0;
		
		regAfectados = "No tiene registros hijos\nFilas afectadas=";
		contador += db.delete("DOCENTE_CARGO", "IDDOCCAR='"
				+ cargoAsignado.getIdDocCar() + "'", null);
		regAfectados += contador;
		return regAfectados;
	}


	public DOCENTE_CARGO consultarDocenteCargo(String idetcurso) {
		String[] id = { idetcurso };
		Cursor cursor = db.query("DOCENTE_CARGO", camposDOCENTE_CARGO,
				"IDDOCCAR = ?", id, null, null, null);
		if (cursor.moveToFirst()) {
			DOCENTE_CARGO cargoAsignado = new DOCENTE_CARGO();
			cargoAsignado.setIdDocCar(cursor.getString(0));
			cargoAsignado.setIdDocente(cursor.getString(1));
			cargoAsignado.setIdPeriodo(cursor.getString(2));
		    cargoAsignado.setIdCargo(cursor.getString(3));
			return cargoAsignado;
		} else {
			return null;
		}
	}

	public String insertar(PERIODO periodo){
			
			String regInsertados="Registro Insertado Nş= ";
			long contador=0;

			
			ContentValues periodos = new ContentValues();
			periodos.put("IDPERIODO", periodo.getIdPeriodo());
			periodos.put("FECHA_FIN", periodo.getFechaIni());
			periodos.put("FECHA_FIN", periodo.getFechaFin());
			contador=db.insert("PERIODO", null, periodos);
			
			if(contador==-1 || contador==0)
			{
				regInsertados= "Error al Insertar el registro, Registro Duplicado. Verificar inserción"+periodo.getIdPeriodo();
			}
			else {
					regInsertados=regInsertados+contador;
			}
			
			return regInsertados;

		}
	public String actualizar(PERIODO periodo) {
		if(verificarIntegridad(periodo, 3)){
		String[] id = { periodo.getIdPeriodo() };
		ContentValues values = new ContentValues();
		values.put("FECHA_INI", periodo.getFechaIni());
		values.put("FECHA_FIN", periodo.getFechaFin());
		db.update("PERIODO", values, "IDPERIODO = ?", id);
		return "Registro actualizado correctamente";
		}else{
			return "Registro con IdPeriodo " + periodo.getIdPeriodo() + " no existe";
		}
	}

	public PERIODO consultarPeriodo(String idPeriodo) {
		String[] id = { idPeriodo };
		Cursor cursor = db.query("PERIODO", camposPERIODO,"IDPERIODO = ?", id, null, null, null);
		if (cursor.moveToFirst()) {
			PERIODO periodo = new PERIODO();
			periodo.setIdPeriodo(cursor.getString(0));
			periodo.setFechaIni(cursor.getString(1));
			periodo.setFechaFin(cursor.getString(2));
			return periodo;
		} else {
			return null;
		}
	}
	public String eliminar(PERIODO periodo){
		String regAfectados="filas afectadas= ";
		int contador=0;
		if (verificarIntegridad(periodo,26)) {
			regAfectados += "Tiene registros hijos\nNo se puede borrar,";
			if (verificarIntegridad(periodo,2))
			regAfectados += " DOCENTE_CARGO tiene registros.";	
			return regAfectados;
		}
		regAfectados = "No tiene registros hijos\nFilas afectadas=";
		contador+=db.delete("PERIODO", "IDPERIODO='"+periodo.getIdPeriodo()+"'", null);
		regAfectados+=contador;
		return regAfectados;	
	}
	
	
	
	public List<String> getAllIdCargos() {
		List<String> idCargos = new ArrayList<String>();
		Cursor cursor = db.rawQuery("select IDCARGO from CARGO order by IDCARGO;",null);
		if (cursor.moveToFirst()) {
			do {
				idCargos.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return idCargos;
	}
	public List<String> getAllIdDocCar() {
		List<String> idDocCargos = new ArrayList<String>();
		Cursor cursor = db
				.rawQuery(
						"select IDDOCCAR from DOCENTE_CARGO order by IDDOCCAR;",
						null);
		if (cursor.moveToFirst()) {
			do {
				idDocCargos.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return idDocCargos;
	}
	public List<String> getAllIdPeriodos() {
		List<String> idPeriodos = new ArrayList<String>();
		Cursor cursor = db.rawQuery("select IDPERIODO from PERIODO order by IDPERIODO;",
						null);
		if (cursor.moveToFirst()) {
			do {
				idPeriodos.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return idPeriodos;
	}
	public List<String> getAllIdDocentes() {
		List<String> idDocentes = new ArrayList<String>();
		Cursor cursor = db	.rawQuery("select IDDOCENTE from DOCENTE order by IDDOCENTE;",null);
		if (cursor.moveToFirst()) {
			do {
				idDocentes.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return idDocentes;
	}

	
		
	/* Verificacion de integridad */
	// FUNCION DE VERIFICACION DE INTEGRIDAD
	// 1 AL 6 aLEXIS
	// 7 AL 12 MARIO
	// 13 AŃ 18 EMERSON
	// 19 AL 24 AGUSTIN
	// 25 AL 31 SERGIO
	private boolean verificarIntegridad(Object dato, int relacion)
			throws SQLException {

		switch (relacion) {
		case 1: {
			// VERIFICAR QUE AL ELIMINAR CICLONO EXISTA REGISTROS HIJOS EN TABLA
			// CARGA_ACADEMICA
			Ciclo ciclo2 = (Ciclo) dato;
			String[] id0 = { "ANIO", "NUMERO" };
			String[] id4 = { ciclo2.getAnio(), ciclo2.getNumero() };
			Cursor c = db.query("CARGA_ACADEMICA", id0,
					"ANIO = ? AND NUMERO = ?", id4, null, null, null);
			if (c.moveToFirst())
				return true;
			else
				return false;

		}

		case 2: {
			// verificar que al insertar CARGA_aCADEMICA exista IDDOCENTE, AŃIO
			// y el
			// CICLO
			Carga_Academica carga = (Carga_Academica) dato;
			String[] id1 = { carga.getIddocente() };
			String[] id2 = { carga.getAnio() };
			String[] id0 = { "ANIO", "NUMERO" };
			String[] id4 = { carga.getAnio(), carga.getNumero() };
			// abrir();
			Cursor cursor1 = db.query("DOCENTE", null, "IDDOCENTE = ?", id1,
					null, null, null);
			Cursor cursor2 = db.query("CICLO", id0, "ANIO = ? AND NUMERO = ?",
					id4, null, null, null);
			// Cursor cursor3 = db.query("CICLO", null, "codmateria = ?",
			// id3,null, null, null);
			if (cursor1.moveToFirst() && cursor2.moveToFirst()) {
				// Se encontraron datos
				return true;
			}
			return false;

		}

		case 3: {
			// VERIFICAR QUE AL ELIMICAR CARGA_ACADEMICA EL REGISTRO NO TENGA
			// HIJOS
			// VERIFICAR QUE AL ELIMINAR CICLONO EXISTA REGISTROS HIJOS EN TABLA
			// CARGA_ACADEMICA
			Carga_Academica carga2 = (Carga_Academica) dato;
			String[] id0 = { "IDDOCENTE", "ANIO", "NUMERO" };
			String[] id4 = { carga2.getIddocente(), carga2.getAnio(),
					carga2.getNumero() };
			Cursor c = db.query("DETALLE_CARGA_ACT_ACAD", id0,
					"IDDOCENTE = ? AND ANIO = ? AND NUMERO = ?", id4, null,
					null, null);
			Cursor d = db.query("DETALLE_CARGA_MAT", id0,
					"IDDOCENTE = ? AND ANIO = ? AND NUMERO = ?", id4, null,
					null, null);
			if (c.moveToFirst() && d.moveToFirst())
				return true;// SE ENCONTRARON REGISTROS HIJOS
			else
				return false;

		}

		case 4: {
			// VERIFICA DUPLICIDAD DE DETALLE_CARGA_MAT AL INSERTAR
			Detalle_Carga_Mat cargamat = (Detalle_Carga_Mat) dato;
			String[] id0 = { "IDDOCENTE", "ANIO", "NUMERO", "IDDETALLECURSO" };
			String[] id4 = { cargamat.getIddocente(), cargamat.getAnio(),
					cargamat.getNumero(), cargamat.getIddetallecurso() };
			// Cursor c = db.query("DETALLE_CARGA_ACT_ACAD", id0,
			// "IDDOCENTE = ? AND ANIO = ? AND NUMERO = ?",id4,null, null,
			// null);
			Cursor d = db
					.query("DETALLE_CARGA_MAT",
							id0,
							"IDDOCENTE = ? AND ANIO = ? AND NUMERO = ? AND IDDETALLECURSO = ?",
							id4, null, null, null);
			if (d.moveToFirst())
				return true;// SE ENCONTRARON REGISTROS = DUPLICIDAD
			else
				return false;

		}

		case 5: {
			// VERIFICA DUPLICIDAD DE DETALLE_CARGA_ACT_ACAD AL INSERTAR!
			Detalle_Carga_ActAcad cargamat = (Detalle_Carga_ActAcad) dato;
			String[] id0 = { "IDDOCENTE", "ANIO", "NUMERO", "IDACTACAD" };
			String[] id4 = { cargamat.getIddocente(), cargamat.getAnio(),
					cargamat.getNumero(), cargamat.getIdactacad() };
			Cursor d = db
					.query("DETALLE_CARGA_ACT_ACAD",
							id0,
							"IDDOCENTE = ? AND ANIO = ? AND NUMERO = ? AND IDACTACAD = ?",
							id4, null, null, null);
			if (d.moveToFirst())
				return true;// SE ENCONTRARON REGISTROS = DUPLICIDAD
			else
				return false;
		}

		case 6: {
			return true;
		}
		case 7: {
			Departamento departamento = (Departamento) dato;
			Cursor cursor = db.query(true, "DOCENTE_DPTO",
					new String[] { "IDDEPARTAMENTO" }, "IDDEPARTAMENTO='"
							+ departamento.getIddepartamento() + "'", null,
					null, null, null, null);
			if (cursor.moveToFirst())
				return true;
			else
				return false;
		}
		case 8: {
			Departamento departamento = (Departamento) dato;
			Cursor cursor = db.query(true, "AREA_MATERIA",
					new String[] { "IDDEPARTAMENTO" }, "IDDEPARTAMENTO='"
							+ departamento.getIddepartamento() + "'", null,
					null, null, null, null);
			if (cursor.moveToFirst())
				return true;
			else
				return false;
		}
		case 9: {
			Materia materia = (Materia) dato;
			Cursor cursor = db.query(true, "DETALLE_GRUPO_ASIGNADO",
					new String[] { "CODIGOMATERIA" }, "CODIGOMATERIA='"
							+ materia.getCodigomateria() + "'", null, null,
					null, null, null);
			if (cursor.moveToFirst())
				return true;
			else
				return false;
		}
		case 10: {
			Materia materia = (Materia) dato;
			Cursor cursor = db.query(true, "AREA_MATERIA",
					new String[] { "CODIGOMATERIA" }, "CODIGOMATERIA='"
							+ materia.getCodigomateria() + "'", null, null,
					null, null, null);
			if (cursor.moveToFirst())
				return true;
			else
				return false;
		}
		case 11: {
			DetalleGrupoAsignado grupoAsignado = (DetalleGrupoAsignado) dato;
			Cursor cursor = db.query(true, "DETALLE_CARGA_MAT",
					new String[] { "IDDETALLECURSO" }, "IDDETALLECURSO='"
							+ grupoAsignado.getIddetallecurso() + "'", null,
					null, null, null, null);
			if (cursor.moveToFirst())
				return true;
			else
				return false;
		}
		case 12: {
			return true;
		}
		/*YO*/
		case 13: {
			TipoContrato contrato = (TipoContrato) dato;
			Cursor cursor = db.query(true, "DOCENTE",
					new String[] { "IDCONTRATO" }, "IDCONTRATO='"
							+ contrato.getIdContrato() + "'", null, null,
					null, null, null);
			if (cursor.moveToFirst())
				return true;
			else
			return false;
		}
		
		case 14: {
			Docente docente = (Docente) dato;
			Cursor cursor = db.query(true, "DOCENTE_CARGO",
					new String[] { "IDDOCENTE" }, "IDDOCENTE='"
							+ docente.getIdDocente() + "'", null, null,
					null, null, null);
			if (cursor.moveToFirst())
				return true;
			else {
				cursor = db.query(true, "DOCENTE_DPTO",
						new String[] { "IDDOCENTE" }, "IDDOCENTE='"
								+ docente.getIdDocente() + "'", null, null,
						null, null, null);
				if (cursor.moveToFirst())
					return true;
				else{
					cursor = db.query(true, "MAT_AREA_PUEDE_IMPARTIR",
							new String[] { "IDDOCENTE" }, "IDDOCENTE='"
									+ docente.getIdDocente() + "'", null, null,
							null, null, null);
					if (cursor.moveToFirst())
						return true;
					else
						return false;
				}
			}
			
		}
		
		
		/*Fin YO*/
		
		case 15: {
			return true;
		}
		
		case 16: {
			return true;
		}
		
		case 17: {
			return true;
		}
		
		case 18: {
			return true;
		}
		
		case 19: {
			Locales local = (Locales) dato;			
			Cursor cursor = db.query(true, "DETALLE_GRUPO_ASIGNADO",
					new String[] { "IDLOCAL" }, "IDLOCAL='"
							+ local.getIdlocal() + "'", null, null,
					null, null, null);
			if (cursor.moveToFirst())
				return true;
			else
				return false;
		}

		case 20: {			
			Actividad_Academica ActAcad = (Actividad_Academica) dato;			
			Cursor cursor = db.query(true, "DETALLE_CARGA_ACT_ACAD",
					new String[] { " IDACTACAD" }, " IDACTACAD='"
							+ ActAcad.getIdactacad() + "'", null, null,
					null, null, null);
			if (cursor.moveToFirst())
				return true;
			else
				return false;
		}
		case 21: {			
			Modalidad_Act_Acad ModActAcademica = (Modalidad_Act_Acad) dato;
			Cursor cursor = db.query(true, "ACTIVIDAD_ACADEMICA",
					new String[] { " IDMODALIDAD " }, " IDMODALIDAD ='"
							+ ModActAcademica.getIdmodalidad() + "'", null, null,
					null, null, null);
			if (cursor.moveToFirst())
				return true;
			else
				return false;
		}
		
		case 22: {
			Modalidad_Curso ModCurso = (Modalidad_Curso) dato;
			Cursor cursor = db.query(true, "DETALLE_GRUPO_ASIGNADO",
					new String[] { "IDMODALIDAD" }, "IDMODALIDAD='"
							+ ModCurso.getIdmodalidadCurso() + "'", null, null,
					null, null, null);
			if (cursor.moveToFirst())
				return true;
			else
				return false;
		}
		
		case 23: {
			return true;
		}
		
		case 24: {
			return true;
		}
		//SERGIO
		case 25: {
			CARGO cargo = (CARGO) dato;
			Cursor cursor = db.query(true, "DOCENTE_CARGO",
					new String[] { "IDCARGO" }, "IDCARGO='"
							+ cargo.getIdCargo() + "'", null,
					null, null, null, null);
			if (cursor.moveToFirst())
				return true;
			else
				return false;
				
		}
		case 26: {
			PERIODO periodo = (PERIODO)dato;
			Cursor cursor=db.query(true, "DOCENTE_CARGO", new String[] {
					"IDPERIODO" }, "IDPERIODO='"+periodo.getIdPeriodo()+"'",null, null, null, null, null);
			if(cursor.moveToFirst())
			return true;
			else
			return false;	
		}
		
		case 27: {
			//verificar que exista Periodo
			PERIODO periodo2 = (PERIODO)dato;
			String[] idp = {periodo2.getIdPeriodo()};
			abrir();
			Cursor cursor = db.query("PERIODO", null, "IDPERIODO = ?", idp, null, null, null);
			if(cursor.moveToFirst()){
				
				return true;
			}			
		}
		case 28: {
			// Verificar que exista cargo
			CARGO cargo2 = (CARGO)dato;
			String[] id = {cargo2.getIdCargo()};
			abrir();
			Cursor cursor = db.query("CARGO", null, "IdCargo = ?", id, null, null, null);
			if(cursor.moveToFirst()){
				
				return true;
			}		
		}
		
		default:
			return false;

		}

	}

	public String llenarBD() {
		db.execSQL("DELETE FROM CARGO");
		db.execSQL("DELETE FROM PERIODO");
		db.execSQL("DELETE FROM TIPO_CONTRATO");
		db.execSQL("DELETE FROM MODALIDAD_ACT_ACAD");
		db.execSQL("DELETE FROM CICLO");
		db.execSQL("DELETE FROM departamento");
		db.execSQL("DELETE FROM materia");
		db.execSQL("DELETE FROM LOCALES");
		db.execSQL("DELETE FROM modalidad_curso");
		db.execSQL("DELETE FROM docente_cargo");
		db.execSQL("DELETE FROM area_materia");
		db.execSQL("DELETE FROM carga_academica");
		db.execSQL("DELETE FROM detalle_grupo_asignado");
		db.execSQL("DELETE FROM detalle_carga_mat");
		db.execSQL("DELETE FROM detalle_carga_act_acad");
		db.execSQL("DELETE FROM ACTIVIDAD_ACADEMICA");
		db.execSQL("DELETE FROM DOCENTE");
		
		db.execSQL("INSERT INTO  CARGO VALUES('JFE04','Jefe de la Unidad');");
		db.execSQL("INSERT INTO  CARGO VALUES('COR02','Coordinador');");
		db.execSQL("INSERT INTO  CARGO VALUES('ACR04','Acesor');");

		db.execSQL("INSERT INTO  PERIODO VALUES('AAC01','15/01/2013','15/12/2013');");
		db.execSQL("INSERT INTO  PERIODO VALUES('CAC02','15/01/2013','15/07/2013');");
		db.execSQL("INSERT INTO  PERIODO VALUES('PER03','15/01/2010','15/12/2013');");

		db.execSQL("INSERT INTO TIPO_CONTRATO VALUES('0001','Tiempo Completo','8');");
		db.execSQL("INSERT INTO TIPO_CONTRATO VALUES('0002','Medio Tiempo','4');");

		db.execSQL("insert into MODALIDAD_ACT_ACAD VALUES('MA01', 'Modalidad1', 3);");
		db.execSQL("insert into MODALIDAD_ACT_ACAD VALUES('MA02', 'Modalidad2', 2);");
		db.execSQL("insert into MODALIDAD_ACT_ACAD VALUES('MA03', 'Modalidad3', 1);");

		db.execSQL("INSERT INTO CICLO VALUES('2012','1','20/02/2012','25/06/2012');");
		db.execSQL("INSERT INTO CICLO VALUES('2013','1','25/02/2013','01/07/2013');");
		db.execSQL("INSERT INTO CICLO VALUES('2013','2','16/07/2013','26/11/2013');");

		db.execSQL("insert into departamento values('001','Administracion');");
		db.execSQL("insert into departamento values('002','Desarrollo de sistemas');");
		db.execSQL("insert into departamento values('003','Programacion');");
		db.execSQL("insert into departamento values('004','Comunicaciones');");

		db.execSQL("insert into materia values('PDM115','Prog moviles');");
		db.execSQL("insert into materia values('BAD115','Bases de datos');");
		db.execSQL("insert into materia values('RHU115','Recursos humanos');");
		db.execSQL("insert into materia values('SIG115','Sistemas gerenciales');");

		db.execSQL("insert into Locales values('A340', 340);");
		db.execSQL("insert into Locales values('B11', 100);");
		db.execSQL("insert into Locales values('D11', 100);");
		db.execSQL("insert into Locales values('B31', 100);");

		db.execSQL("insert into MODALIDAD_CURSO VALUES('M001', 'completa', 5);");
		db.execSQL("insert into MODALIDAD_CURSO VALUES('M002', 'media', 3);");
		db.execSQL("insert into MODALIDAD_CURSO VALUES('M003', 'corta', 1);");

		//TABLAS PADRE

		db.execSQL("insert into area_materia values('001','001','RHU115');");
		db.execSQL("insert into area_materia values('002','003','PDM115');");
		db.execSQL("insert into area_materia values('003','003','BAD115');");

		db.execSQL("insert into detalle_grupo_asignado values('001','RHU115','M001','B11');");
		db.execSQL("insert into detalle_grupo_asignado values('002','PDM115','M003','D11');");
		db.execSQL("insert into detalle_grupo_asignado values('003','BAD115','M002','B31');");

		
		db.execSQL("insert into DOCENTE values('PP02001','0001','Juan Jose','Perez Perez','Ingeniero','juan.perez@ues.edu.sv','22581010',8);");
		db.execSQL("insert into DOCENTE values('AS00001','0002','Benito Eliseo','Araujo Sanchez','Ingeniero','benito.araujo@ues.edu.sv','22581015',4);");
				
		db.execSQL("insert into carga_academica values('PP02001','2012','1');");
		db.execSQL("insert into carga_academica values('PP02001','2013','1');");
		db.execSQL("insert into carga_academica values('AS00001','2013','2');");
		
		
		db.execSQL("insert into ACTIVIDAD_ACADEMICA values('A001','MA01','Actividad1','Cargo1');");
		db.execSQL("insert into ACTIVIDAD_ACADEMICA values('A002','MA02','Actividad2','Cargo2');");
		db.execSQL("insert into ACTIVIDAD_ACADEMICA values('A003','MA03','Actividad3','Cargo3');");
		
		db.execSQL("insert into detalle_grupo_asignado values('G001','PDM115','M001','B11');");
		db.execSQL("insert into detalle_grupo_asignado values('G002','BAD115','M002','B11');");
		db.execSQL("insert into detalle_grupo_asignado values('G003','SIG115','M003','D11');");
		
		db.execSQL("insert into docente_cargo values('DC001','AS00001','AAC01','ACR04');");
		
		//db.execSQL("insert into detalle_carga_mat values('PP02001','2012','1','G001');");
		//db.execSQL("insert into detalle_carga_mat values('PP02001','2013','1','G002');");
		//db.execSQL("insert into detalle_carga_mat values('AS00001','2013','2','G003');");
		
		//db.execSQL("insert into detalle_carga_act_acad values('PP02001','2012','1','act001');");
		//db.execSQL("insert into detalle_carga_act_acad values('PP02001','2013','1','act002');");
		//db.execSQL("insert into detalle_carga_act_acad values('AS00001','2013','2','act003');");
		
	
					
		return "Guardo Correctamente";
	}//fin funcion llenar BD

}

