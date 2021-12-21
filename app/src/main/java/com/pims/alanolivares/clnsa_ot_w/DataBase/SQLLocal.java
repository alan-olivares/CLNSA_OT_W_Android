package com.pims.alanolivares.clnsa_ot_w.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLLocal  extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "AAB_CLNSA.db";
    public static final String MAIN_TABLES[]={"CM_Alcohol","CM_CodEdad","CM_Codificacion","CM_EstadoLote","CM_Edad"};

    public SQLLocal(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS AA_Almacen(AlmacenID int NOT NULL,Nombre varchar(30),Descripcion varchar(50),Consecutivo int NOT NULL,PlantaID int )");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS AA_Area(AreaId bigint NOT NULL,AlmacenId int NOT NULL,Nombre varchar(30),Consecutivo int NOT NULL)");
        //sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS AA_Impresion(Idimpresion bigint NOT NULL,IdPlanta tinyint,IdRecurso tinyint,IdAsText nchar(10))");
        //sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS AA_ImpresionHist(IdReimpresion bigint NOT NULL,Idimpresion bigint,IDUSUARIO int,Fecha datetime)");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS AA_Nivel(NivelID int NOT NULL,PosicionId int NOT NULL,Nombre varchar(30),Consecutivo int NOT NULL)");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS AA_Plantas(PlantaID int NOT NULL,Nombre varchar(30),Descripcion varchar(50),Consecutivo int NOT NULL)");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS AA_Posicion(PosicionID int NOT NULL,SeccionID int NOT NULL,Nombre varchar(30),Consecutivo int NOT NULL)");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS AA_Seccion(SeccionID int NOT NULL,AreaId int NOT NULL,Nombre varchar(30),Consecutivo int NOT NULL)");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS CM_Alcohol(IdAlcohol int NOT NULL,Codigo varchar(20),Descripcion varchar(50),Grado float,Observaciones varchar(100))");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS CM_CodEdad(IdCodEdad int NOT NULL,IdCodificicacion int NOT NULL,IdEdad int NOT NULL)");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS CM_Codificacion(IdCodificacion int NOT NULL,Codigo varchar(30),Descripcion varchar(50))");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS CM_Edad(IdEdad int,Codigo varchar(20),Descripcion varchar(50))");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS CM_EstadoLote(IdEstadoLote int NOT NULL,Descripcion varchar(50))");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS CM_Grupo(idGrupo int NOT NULL,Nombre varchar(80))");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS CM_MovtoOp(IdMovtoOP bigint NOT NULL,IdTipoOp bigint NOT NULL,Descripcion varchar(50))");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS CM_Planta(IdPlanta tinyint NOT NULL,Descripcion varchar(120))");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS CM_Proveedor(IdProveedor int NOT NULL,Codigo varchar(30),Descripcion varchar(50))");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS CM_Recurso(IdRecurso int NOT NULL,Descripcion varchar(50))");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS CM_Scanner(IdScan int NOT NULL,Numero int NOT NULL,Descripcion varchar(100),Estatus int,ModSinc int,PantSinc int,IdUsuario int,FechaAsignada datetime)");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS CM_Tanque(IDTanque int NOT NULL,Codigo varchar(50),Descripcion varchar(100),Capacidad float,Tipo int)");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS CM_TipoOp(IdTipoOP int NOT NULL,Descripcion varchar(50),Indexy int)");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS cm_SyncBodega(idbodega int PRIMARY KEY, usuario nvarchar(80),fecha datetime)");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS PR_Lote(IdLote bigint NOT NULL,IdRecurso int NOT NULL,IDEstadoLote int NOT NULL,IdAlcohol int NOT NULL,Recepcion datetime NOT NULL,Liberacion datetime)");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS PR_NvUbicacion(Consecutivo int,Racklocfin int,tipo int)");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS PR_Op(IdOperacion bigint  NOT NULL,IdOrden bigint NOT NULL,Cantidad float,Estatus int,Fecha varchar(10),IdAlcohol int,IdCodificacion int,IdTanque int,IdLote bigint,AreaID bigint,SeccionID bigint)");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS PR_Operacion(IdOperacion bigint NOT NULL,IdOrden bigint NOT NULL,Cantidad float,Estatus int,Fecha varchar(10),IdAlcohol int,IdCodEdad int,IdTanque int,IdLote bigint)");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS PR_OperaDetail(IdOperaDetail bigint  NOT NULL,IdOperacion bigint NOT NULL,IdBarrica bigint NOT NULL,Capacidad float,Estatus int)");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS PR_Orden(IdOrden bigint NOT NULL,IdTipoOp int NOT NULL,IdLote bigint NOT NULL,IdAlmacen bigint NOT NULL,IdArea bigint NOT NULL,IdSeccion bigint NOT NULL,IdOperario int,IdOperarioMon int,IdSupervisor int,Fecha datetime NOT NULL,Estatus int,IdUsuario int)");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS WM_Barrica (IdBarrica bigint,IdPallet bigint,IdLoteBarrica bigint,Consecutivo bigint,IdEstado int,Capacidad float,IdCodificacion int,Fecha datetime,FechaRevisado datetime,FechaRelleno datetime,Merma float,Año nvarchar(100),NoTapa int)");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS wm_barril (consecutivo bigint,idpallet bigint,estado tinyint,tipo tinyint,tapa int,anio int,Litros float,lote bigint,alcohol tinyint,recepcion datetime,revisado datetime,relleno datetime,bodega tinyint,costado int,fila int,torre int,nivel int)");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS WM_LoteBarrica (IdLoteBarica bigint NOT NULL ,IdProveedor int,IdUsuario int NOT NULL,IdLote bigint, Cantidad float, OrdenCompra nvarchar(50) NOT NULL,Fecha datetime NOT NULL)");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS WM_Pallet (idpallet bigint NOT NULL,RacklocID bigint,Estatus int,Pistola tinyint)");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS WM_RackLoc (RackLocID bigint NOT NULL,NivelID bigint,ClasifiID int)");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS WM_Recurso (IdRecurso int NOT NULL, Consecutivo int,Descripcion nvarchar(50))");
        //Tabla de notificaciones
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS notifiOrden (idnotifiOrden bigint, Fecha datetime)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}
