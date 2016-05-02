/* =================================================================
Copyright (C) 2009 ADV/web-engineering All rights reserved.

This file is part of Mozart.

Mozart is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Mozart is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

Mozart
http://www.mozartcms.ru
================================================================= */
//-*- java -*-
//File: ADVException.java
//
//Created: Mon Jul  8 14:57:20 2002
//
//$Id: ADVExceptionCode.java 1333 2009-09-18 05:56:35Z vic $
//$Name:  $
//

package ru.adv.util;

/**
 * Define types of ADVException
 * 
 */
public interface ADVExceptionCode {
    /** general exception type, default * */
    int UNKNOWN_ERROR                                = 0;
    /** Bad value* */
    int INVALID_VALUE_TYPE                           = 1001;
    int SEVERAL_UNIQUE_GROUPS_FOUND                  = 1003;
    int NO_UNIQUE_GROUP_FOUND                        = 1004;
    int REQUIRED_ATTRIBUTE                           = 1005;
    int INVALID_FOREIGN                              = 1006;
    int UNIQUENESS_VIOLATION                         = 1007;
    int OBJECT_NOT_FOUND                             = 1010;
    int INVALID_BOOLEAN_VALUE                        = 1011;
    int INVALID_INT_VALUE                            = 1012;
    int INVALID_LONG_INT_VALUE                       = 1013;
    int INVALID_SHORT_INT_VALUE                      = 1014;
    int INVALID_BYTE_VALUE                           = 1015;
    int INVALID_FLOAT_VALUE                          = 1016;
    int INVALID_DOUBLE_VALUE                         = 1017;
    int INVALID_DATE_VALUE                           = 1018;
    int INVALID_ATTRIBUTE                            = 1019;
    int INVALID_PATTERN                              = 1020;
    int EMPTY_MATCH_PATTERN                          = 1021;
    int INVALID_IP_ADDRESS                           = 1022;
    int INVALID_DATE_FORMAT                          = 1030;
    int NEWT_IS_NOT_REGISTERED                       = 1040;
    int INVALID_XPATH_EXPRESSION                     = 1041;
    int INVALID_EXPIRES                              = 1042;
    int INVALID_QUERY_FILTER                         = 1043;
    int INVALID_ADD_QUERY                            = 1044;
    int NEWT_IS_NOT_IN_CACHE                         = 1045;

    int DEEP_RECURSION                               = 1046;

    // DBNEWT
    int DATABASE_NOT_SPECIFIED                       = 1050;

    int UNKNOWN_DATABASE                             = 1051;

    int UNKNOWN_DATABASE_USER                        = 1052;

    int CANNOT_FIND_DEFAULT_USER                     = 1053;

    int PASSWORD_REQUIRED                            = 1054;

    int UNKNOWN_FILTER_MAP                           = 1055;

    int ORPHAN_FILTER_MAP                            = 1056;

    // GENERIC
    int CLASS_IS_NOT_NEWT                            = 1060;

    // factory
    int CANNOT_INSTANTIATE                           = 1061;

    int ILLEGAL_ACCESS                               = 1062;

    int CLASS_NOT_FOUND                              = 1063;

    int UNKNOWN_TYPE                                 = 1064;

    int CANNOT_VERIFY_DOCUMENT                       = 1065;

    int CACHE_ERROR                                  = 1066;

    int CANNOT_PROCESS_NEWTS                         = 1067;

    int CANNOT_PARSE_MATRIX                          = 1068;

    int DB_UNKNOWN_ATTRIBUTE                         = 1070;

    int DB_CANNOT_CREATE_HANDLER                     = 1071;

    int DB_CANNOT_GET_CONNECT_FROM_POOL              = 1072;

    int DB_CANNOT_GET_AUTOCOMMIT_STATUS              = 1073;

    int DB_CANNOT_SET_AUTOCOMMIT_STATUS              = 1074;

    int DB_CANNOT_COMMIT                             = 1075;

    int DB_CANNOT_ROLLBACK                           = 1076;

    int DB_CANNOT_SELECT                             = 1077;

    int DB_CANNOT_DELETE                             = 1078;

    int DB_CANNOT_FIND                               = 1079;

    int DB_CANNOT_INSERT                             = 1080;

    int DB_CANNOT_UPDATE                             = 1081;

    int DB_UNDEFINED_FILE_STORAGE                    = 1083;

    // TODO
    int DB_TREE_CYCLE                                = 1084;

    // TODO
    int DB_TREE_PARENT_EXISTS                        = 1085;

    int DB_CANNOT_INCREMENT_SEQUENCE                 = 1086;

    int DB_CANNOT_GET_CURRENT_ID                     = 1087;

    int DB_CANNOT_READ_TREE                          = 1088;

    int DB_CANNOT_CREATE_SQL                         = 1089;

    int DB_CANNOT_CREATE_SQL_WHERE                   = 1090;

    int DB_INVALID_SORT_STRING                       = 1091;

    int DB_TEMPORARY_UNAVAILABLE                     = 1092;

    int MOZART_CANNOT_LOAD_DB_DESCRIPTOR             = 1095;

    int MOZART_CANNOT_LOAD_SITETREE                  = 1096;

    int MOZART_CANNOT_CONFIG                         = 1097;

    int MOZART_AUTH_ERROR                            = 1098;

    int IO_UNKNOWN_SOURCE                            = 1101;

    int IO_ERROR                                     = 1102;

    int IO_CANNOT_CREATE                             = 1103;

    int IO_CANNOT_REMOVE                             = 1104;

    int IO_CANNOT_COPY                               = 1105;

    int IO_OUT_OF_ROOT                               = 1106;

    int IO_UNSUPPORTED_ENCODING                      = 1107;

    int ROWSET_CANNOT_CLOSE                          = 2000;

    int ROWSET_CANNOT_MOVE_CURSOR                    = 2001;

    int ROWSET_CANNOT_GET_ROW_NUMBER                 = 2002;

    int ROWSET_CANNOT_FETCH_OBJECT                   = 2003;

    int ROWSET_CANNOT_SELECT                         = 2004;

    // prepare db config @todo
    int PREP_OBJECT_DEFINED_MORE_THAN_ONCE           = 3001;

    int PREP_INVALID_USER_NAME                       = 3002;

    int PREP_MISSING_DBT                             = 3003;

    int PREP_MISSING_TYPE                            = 3004;

    int PREP_ID_TYPE_RESERVED                        = 3005;

    int PREP_MISSING_ID_ATTRIBUTE                    = 3006;

    int PREP_INVALID_ID                              = 3007;

    /** Packet errors * */
    int PACKET_EMPTY_OBJECT_LIST                     = 4001;
    int PACKET_OBJECTS_NOT_LINKED                    = 4002;
    int PACKET_INVALID_SEARCH_ATTRIBUTE              = 4003;
    int PACKET_OBJECT_NOT_FOUND                      = 4004;
    int PACKET_IMPORT_INTERRUPTED                    = 4005;						

    int INVALID_QUERY                                = 5001;

    int SESSION_INVALIDATED                          = 5002;

    // db create
    int DBC_CANT_CREATE_DATABASE                     = 6001;

    int DBC_CANT_INIT_DATABASE                       = 6002;

    int DBC_SYNCRONIZE_ATTRIBUTE_ERROR               = 6003;

    int DBC_CANT_SELECT_UNSPECIFIED_REQUIREDS        = 6004;

    int DBC_REQUIRED_FILE_ATRRIBUTE_CANT_BE_FALSE    = 6005;

    int DBC_CANT_GET_TABLE_COLUMNS                   = 6006;

    int DBC_UNKNOW_OBJECT                            = 6007;

    int DBC_CANT_ADD_COLUMN                          = 6008;

    int DBC_DEFAULT_VALUES_NEEDED                    = 6009;

    int DBC_CANT_UPDATE_EMPTY_REQUIREDS              = 6011;

    int DBC_CANT_UPDATE_FOREIGNS                     = 6012;

    int DBC_CANT_SELECT_FOREIGNS                     = 6013;

    int DBC_CANT_OPEN_FILE                           = 6014;

    int DBC_CANT_REMOVE_FILE                         = 6015;

    int DBC_CANT_DROP_ATTRIBUTES                     = 6016;

    int DBC_CANT_SYNCRONIZE_COLUMNS_TYPES            = 6017;

    int DBC_CANT_CREATE_CONSTRAINS                   = 6018;

    int DBC_CANT_DROP_CONSTRAINS                     = 6019;

    int DBC_CANT_CREATE_FUNCTION                     = 6020;

    int DBC_CANT_DROP_FUNCTION                       = 6021;

    int DBC_CANT_DROP_FUNCTIONS                      = 6022;

    int DBC_CANT_REGISTER_FUNCTION                   = 6023;

    int DBC_CANT_UNREGISTER_FUNCTION                 = 6024;

    int DBC_CANT_CREATE_EXT_TREE                     = 6025;

    int DBC_CANT_DROP_EXT_TREE                       = 6026;

    int DBC_CANT_REGISTER_EXT_TREE                   = 6027;

    int DBC_CANT_UNREGISTER_EXT_TREE                 = 6028;

    int DBC_CANT_REFILL_EXT_TREE                     = 6029;

    int DBC_CANT_CREATE_OBJECT                       = 6030;

    int DBC_CANT_DROP_OBJECT                         = 6031;

    int DBC_CANT_REGISTER_OBJECT                     = 6032;

    int DBC_CANT_UNREGISTER_OBJECT                   = 6033;

    int DBC_CANT_GET_LIST_OF_OBJECTS                 = 6034;

    int DBC_CANT_DROP_OBJECTS                        = 6035;

    int DBC_CANT_GET_LIST_OF_TRIGGERS                = 6036;

    int DBC_CANT_CREATE_TRIGGER                      = 6037;

    int DBC_CANT_DROP_TRIGGER                        = 6038;

    int DBC_CANT_REGISTER_TRIGGER                    = 6039;

    int DBC_CANT_UNREGISTER_TRIGGER                  = 6040;

    int DBC_CANT_DROP_TRIGGERS                       = 6041;

    int DBC_CANT_GET_LIST_OF_INDEXES                 = 6042;

    int DBC_CANT_CREATE_INDEX                        = 6043;

    int DBC_CANT_DROP_INDEX                          = 6044;

    int DBC_CANT_REGISTER_INDEX                      = 6045;

    int DBC_CANT_UNREGISTER_INDEX                    = 6046;

    int DBC_CANT_DROP_INDEXES                        = 6047;

    int DBC_CANT_GET_LIST_OF_DATABASES               = 6059;

    int DBC_EMPTY_REPOSITORY_ID                      = 6060;

    int DBC_CHECK_INTEGRITY                          = 6061;

    int DBC_CANNOT_OPEN_DATABASE_CONNECTION          = 6062;

    int DBC_CANNOT_CLOSE_DATABASE_CONNECTION         = 6063;

    int DBC_CANT_DELETE_BADREFERENCED_OBJECT         = 6064;

    int DBC_CANT_DELETE_FOREIGNS                     = 6065;
    
    // Repository
    int REPOZITORY_ALREADY_INITIALIZED               = 8001;
    int REPOZITORY_NOT_INITIALIZED                   = 8002;
    int REPOZITORY_CS_ALREADY_STARTED                = 8003;
    int REPOZITORY_CANNOT_START_CS                   = 8004;
    int REPOSITORY_DATABASE_ALREADY_EXISTS           = 8005;
    int REPOSITORY_DATABASE_NOT_FOUND                = 8006;
    int REPOSITORY_CANNOT_CREATE_LOCK_FILE           = 8007;
    int REPOSITORY_DATABASE_LOCKED                   = 8008;
    int REPOSITORY_DUMP_ALREADY_EXISTS               = 8009;
    int REPOSITORY_DUMP_NOT_FOUND                    = 8010;
    int REPOSITORY_DATABASE_NOT_IN_USE               = 8011;
    int REPOSITORY_CANNOT_CREATE_DUMP                = 8012;
    int REPOSITORY_CANNOT_REMOVE_DUMP                = 8013;
    int REPOSITORY_INVALID_BASE_XML_TEMPLATE         = 8014;
    int REPOSITORY_INVALID_NAME                      = 8015;
    int REPOSITORY_INVALID_DB_CONFIG_FORMAT          = 8016;
    int REPOSITORY_CHANNEL_EXCEPTION                 = 8017;
    int REPOSITORY_CANNOT_DROP_DATABASE              = 8018;
    int REPOSITORY_UNKNOWN_REPLICATION_MODE          = 8019;
    int REPOSITORY_ERROR_ON_UNIQUE_CHECK             = 8020;
    int REPOSITORY_INJECT_WITHOUT_REMOVED_ATTR       = 8021;
    int REPOSITORY_CONFIG_ERROR                      = 8022;
    int REPOSITORY_CANNOT_CREATE_CONNECTION          = 8023;
    int REPOSITORY_CANT_LOCK_DATABASE                = 8024;
    
    int FILTER_ERROR                                 = 10010;
    int FILTER_EMPTY_PARAMETERS                      = 10011;
    int FILTER_CANNOT_RESIZE_IMAGE                   = 10012;
    int FILTER_CANNOT_CREATE_TEMP_FILE               = 10013;
    int FILTER_CANNOT_LOAD_IMAGE                     = 10014;
    int FILTER_INVALID_OBJECT_TYPE                   = 10015;

    int FILTER_CANNOT_SAVE_IMAGE                     = 10016;

    int FILTER_INVALID_PARAMETER                     = 10017;

    int FILTER_INVALID_NUMBER                        = 10018;

    int FILTER_INVALID_DATE_TIME                     = 10019;

    int FILTER_INVALID_TIME_ZONE                     = 10020;

    int FILTER_CANNOT_PARSE_XML                      = 10021;

    int ACTION_CANNOT_FIND_QUESTION                  = 11000;

    int ACTION_INVALID_SUBJECT                       = 11001;

    int ACTION_INVALID_TO_ADDRESS                    = 11002;

    int ACTION_INVALID_CC_ADDRESS                    = 11003;

    int ACTION_INVALID_BCC_ADDRESS                   = 11004;

    int ACTION_INVALID_FROM_ADDRESS                  = 11005;

    int ACTION_CANNOT_CREATE_MIME_MESSAGE            = 11006;

    int ACTION_CANNOT_SEND_MESSAGE                   = 11007;

    int ACTION_CANNOT_GET_MESSAGE_ID                 = 11008;

    int ACTION_CANNOT_ATTACH_FILE                    = 11009;

    int ACTION_CANNOT_PREPARE_MESSAGE_BODY           = 11010;

    int ACTION_FILE_DOES_NOT_EXIST                   = 11011;

    int ACTION_CANNOT_OPEN_TRANSFORMER               = 11012;

    int ACTION_OBJECTS_NOT_DEFINED                   = 11013;

    int ACTION_UNKNOWN_RENEW_OBJECTS                 = 11014;

    int ACTION_UNKNOWN_NEWONLY_OBJECTS               = 11015;

    // newt:form
    int FORM_NO_ANSWER                               = 12003;

    int FORM_CANNOT_TRANSFORM_SUCCESS_ELEMENT        = 12004;

    int FORM_INVALID_HTTP_HEADER_VALUE               = 12005;

    int FORM_INVALID_STATUS_CODE                     = 12006;

    int FORM_EMPTY_COOKIE_NAME                       = 12007;

    int FORM_EMPTY_COOKIE_VALUE                      = 12008;

    int FORM_DUPLICATE_QUIESTION                     = 12010;
    
    int CAPTCHA_SERVICE_EXCEPTION					 = 12015;

    int BASE_NO_REQUEST                              = 14001;

    int BASE_CANNOT_TRANSFORM_REQUEST                = 14002;

    int LINK_CANNOT_SET_TREE_TYPE                    = 14003;

    int REQUEST_INVALID_TREE_MODE                    = 14004;

    int REQUEST_CANNOT_SET_CHILD_SORT                = 14005;

    int REQUEST_NOT_UNIQUE_CHILD                     = 14006;

    int REQUEST_EMPTY_OBJECT                         = 14007;

    int REQUEST_ALREADY_HAS_CHILDREN                 = 14008;

    int REQUEST_UNKNOWN_TREE_MODE                    = 14009;

    int REQUEST_NO_NAME_ATTR                         = 14010;

    int REQUEST_INFO_WITHOUT_PARENT_GET              = 14011;

    int REQUEST_INFO_WITHOUT_ATTR                    = 14012;

    int REQUEST_UNDEFINED_ATTRIBUTES                 = 14013;

    int REQUEST_UNDEFINED_SAVE_OBJECTS               = 14015;

    int REQUEST_INVALID_RENEW_SET                    = 14016;

    int REQUEST_INVALID_NEWONLY_SET                  = 14017;

    int REQUEST_CANNOT_EXCEUTE_SET_CLASS             = 14018;

    int REQUEST_INVALID_SET_CLASS                    = 14019;

    int REQUEST_UNDEFINED_SET_OBJECT                 = 14020;

    int REQUEST_DEFINED_UPDATE_QUERY_AND_VALUE       = 14021;

    int REQUEST_INVALID_CONTEXT_ATTR                 = 14022;

    int REQUEST_INVALID_TREE_ATTR                    = 14023;

    int REQUEST_UNDEFINED_UNSET_OBJECT               = 14024;

    int REQUEST_TIME_HARD_LIMIT                      = 14025;

    int REQUEST_SIZE_HARD_LIMIT                      = 14026;

    int REQUEST_CONTEXT_STRING_TOO_SHORT             = 14027;
    
    int REQUEST_SET_ATTR_DOUBLE                      = 14028;

    int BASE_CONFIG_CANNOT_MATCH                     = 15000;

    int BASE_CONFIG_CANNOT_NODE_MATCH                = 15001;
    
    int CALENDAR_INVALID_TOKEN                       = 16000;

    int CALENDAR_INVALID_DAY                         = 16001;

    int CALENDAR_INVALID_MONTH                       = 16002;

    int CALENDAR_INVALID_WEEKDAY                     = 16003;

    int CALENDAR_INVALID_SYNTAX                      = 16004;

    int CALENDAR_BAD_USAGE                           = 16005;

    int MIME_CANNOT_PARSE_MESSAGE                    = 17000;

    int ASPELL_CANNOT_LOAD_DICTIONARY                = 18000;

    int ASPELL_EMPTY_LANGUAGE_ATTRIBUTE              = 18001;

    int TRANSFORM_CANNOT_INITIALIZE                  = 19000;

    int TRANSFORM_CANNOT_APPLY_TEMPLATE              = 19001;

    int XFORMS_CANNOT_INITIALIZE                     = 20000;

    int XFORMS_CANNOT_CREATE_FORM                    = 20001;

    int XFORMS_CANNOT_DISPATCH_TRIGGER               = 20002;

    int XFORMS_CANNOT_HANDLE_SELECTOR                = 20003;

    int XFORMS_CANNOT_UPDATE_CONTROL                 = 20004;

    int XFORMS_EMPTY_ID                              = 20005;

    int DBCONFIG_INVALID_IDENTIFICATOR               = 21000;

    int DBCONFIG_UNKNOWN_OBJECT                      = 21001;

    int DBCONFIG_CYCLE                               = 21002;

    int DBCONFIG_NO_BASE_ELEMENT                     = 21003;

    int DBCONFIG_UNKNOWN_DB_ADAPTER                  = 21004;

    int DBCONFIG_ILLEGAL_DEFINITION_OF_VERSION_ATTR  = 21005;

    int DBCONFIG_NO_VERSION_ATTR                     = 21006;

    int DBCONFIG_LINK_NAME_ALREADY_EXISTS            = 21007;

    int DBCONFIG_DUPLICATE_LINK                      = 21008;

    int DBCONFIG_INVALID_DEPEND_OBJECTS_ATTR         = 21009;

    int DBCONFIG_INVALID_SORT_ATTR                   = 21009;

    int DBCONFIG_FILTER_NOT_FOUND                    = 21010;

    int DBCONFIG_OBJECT_NOT_FOUND                    = 21011;

    int DBCONFIG_ATTRIBUTE_NOT_FOUND                 = 21012;

    int DBCONFIG_OBJECT_IDENTIFICATOR_TOO_LONG       = 21013;

    int DBCONFIG_OBJECT_IS_NOT_TREE                  = 21014;

    int DBCONFIG_INVALID_AUTO_EXPIRES_ATTR           = 21015;

    int DBCONFIG_NO_ID_ATTR                          = 21016;

    int DBCONFIG_NO_DBT_ATTR                         = 21017;

    int DBCONFIG_CASE_INSENSITIVE_SORT_NOT_SUPPORTED = 21018;

    int DBCONFIG_INVALID_VIEW_NAME                   = 21019;

    int DBCONFIG_TIGGER_NOT_FOUND                    = 21020;

    int DBCONFIG_INDEX_NOT_FOUND                     = 21021;

    int DBCONFIG_EMPTY_INDEX_ID                      = 21022;

    int DBCONFIG_EMPTY_INDEX                         = 21023;

    int DBCONFIG_EMPTY_DEFAULT_VALUE                 = 21024;

    int DBCONFIG_INVALID_DEFAULT_VALUE               = 21025;

    int DBCONFIG_ATTRIBUTE_IDENTIFICATOR_TOO_LONG    = 21026;

    int DBCONFIG_DIPLICATE_TRIGGER_VARIABLE          = 21027;

    int DBCONFIG_INVALID_TRIGGER_VARIABLE            = 21028;

    int DBCONFIG_INVALID_TRIGGER_EVENT               = 21029;

    int DBCONFIG_INVALID_LINK                        = 21031;

    int DBCONFIG_TREE_OBJECT_IN_LINK                 = 21032;

    int DBCONFIG_EMPTY_LINK                          = 21033;

    int DBCONFIG_ILLEGAL_PRIVILEGE_ACTION            = 21034;

    int DBCONFIG_CANNOT_PARSE                        = 21035;

    int DBCONFIG_CANNOT_PREPARE                      = 21036;

    int DBCONFIG_ILLEGAL_DEFINITION_OF_REMOVED_ATTR  = 21037;

    int DBCONFIG_NO_REMOVED_ATTR                     = 21038;
    
    int DBCONFIG_ADAPTER_NOT_SUPPORT_NATIVE_TRIGGERS = 21039;
    
    int DBCONFIG_GROOVY_COMPILE                      = 21040;
    
    int NEWT_ACTION_CONTROLLER_FACTORY_NOT_DEFINED	= 22001;
    int NEWT_ACTION_CONTROLLER_BAD_ATTRS			= 22002;
    int NEWT_ACTION_CONTROLLER_INIT					= 22003;
    int NEWT_EL_NOT_INITED                          = 22004;

    int CONTROLLER_NOT_FOUND						= 23001;
    int CONTROLLER_INIT								= 23002;
    int CONTROLLER_EXECUTION						= 23003;
    
    int NEWT_MAIL_NO_ELEMENTS						= 24001;
    int NEWT_MAIL_CANNOT_CREATE_MIME_MESSAGE		= 24002;
    int NEWT_MAIL_FORMAT_ERROR						= 24003;
    int NEWT_MAIL_CANNOT_SEND_MESSAGE				= 24004;
    int NEWT_MAIL_CANNOT_PARSE_XSL					= 24005;
    int NEWT_MAIL_CANNOT_APPLY_XSL					= 24006;
    int NEWT_MAIL_CANNOT_RUN_NEWTS					= 24007;
    int NEWT_MAIL_CANNOT_GET_TEMPLATE				= 24008;
    int NEWT_MAIL_CANNOT_PARSE_TEMPLATE				= 24009;
    int NEWT_MAIL_CANNOT_GET_SRC					= 24010;
    int NEWT_MAIL_UNKNOWN_ERROR						= 24011;
    int NEWT_MAIL_DOM_ERROR							= 24012;
    int NEWT_MAIL_CANNOT_FIND_CONFIG_ELEMENT		= 24013;
    int NEWT_MAIL_TRANSFORM_CANNOT_APPLY_TEMPLATE	= 24014;
    
    int MAIL_TASK_CANNOT_FIND_DOCUMENT_INFO			= 25001;
    int MAIL_TASK_CANNOT_LOAD_SITETREE				= 25002;
    int MAIL_TASK_CANNOT_READ_CONFIG_FILE			= 25003;
    int MAIL_TASK_CANNOT_PARSE_CONFIG_FILE			= 25004;
    int MAIL_CONFIG_CANNOT_FIND_SOURCE				= 25005;
    int MAIL_CONFIG_CANNOT_FIND_MAPPING				= 25006;
    int MAIL_CONFIG_INVALID_TYPE_ATTRIBUTE_VALUE	= 25007;
    int MAIL_CONFIG_CANNOT_FIND_TYPE_ATTRIBUTE		= 25008;
    int MAIL_CONFIG_UNKNOWN_FORMAT_VALUE			= 25009;
    int MAIL_CONFIG_CANNOT_FIND_FORMAT_ATTRIBUTE	= 25010;
    int MAIL_CONFIG_INVALID_DATABASE_VALUE			= 25011;
    int MAIL_CONFIG_CANNOT_FIND_DATABASE_ATTR		= 25012;
    int MAIL_CONFIG_INVALID_CONTROLLER_ATTR			= 25013;
    int MAIL_CONFIG_CANNOT_FIND_MAPPING_OBJECTS		= 25014;
    int MAIL_CONFIG_CANNOT_FIND_MAPPING_ATTRIBUTES	= 25015;
    int MAIL_CONFIG_CANNOT_FIND_GET_INSTUCTION		= 25016;
    int MAIL_SENDER_CANNOT_CREATE_MIME_MESSAGE		= 25017;
    int MAIL_SENDER_CANNOT_SEND_MESSAGE				= 25018;
    int MAIL_SENDER_FORMAT_ERROR					= 25019;
    int MAIL_SENDER_CANNOT_GET_TEMPLATE				= 25020;
    int MAIL_SENDER_CANNOT_PARSE_TEMPLATE			= 25021;
    int MAIL_SENDER_CANNOT_RUN_NEWTS				= 25022;
    int MAIL_SENDER_CANNOT_PARSE_XSL				= 25023;
    int MAIL_SENDER_CANNOT_APPLY_XSL				= 25024;
    int MAIL_SENDER_CANNOT_GET_XSL					= 25025;
    int MAIL_SENDER_TRANSFORM_CANNOT_APPLY_TEMPLATE	= 25026;
    int MAIL_SENDER_CANNOT_GET_DATABASE_DESCRIPTOR	= 25027;
    int MAIL_SENDER_CANNOT_GET_REPOSITORY			= 25028;
    int MAIL_SENDER_REPOSITORY_NOT_INITIALIZED		= 25029;
    int MAIL_SENDER_CANNOT_GET_CSV_FILE				= 25030;
    int MAIL_SENDER_BAD_ENCODING					= 25031;
    int MAIL_SENDER_REPOSITORY_ERROR				= 25032;
    int MAIL_SENDER_CANNOT_FIND_REPOSITORY			= 25033;
    int MAIL_SENDER_CANNOT_FIND_DATABASE			= 25034;
    int MAIL_SENDER_CANNOT_SET_SECURITY_OPTIONS		= 25035;

    int ACCESS_DENIES                                = 50000;

}

