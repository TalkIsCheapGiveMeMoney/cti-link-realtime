package com.tinet.ctilink.realtime.inc;

/**
 * @Title IvrStaticParameters.java
 * @Package com.tinet.ccic.ivr.formattool
 * @category IVR模块通道变量名的static final设置，统一管理
 * @author mucw
 */
public class IvrStaticParameters {
	/**通道变量:enterprise_status 企业目前业务状态*/
	public static final String ENTERPRISE_CALL_LIMIT_IB = "enterprise_call_limit_ib";
	
	/**通道变量:enterprise_status 企业目前业务状态*/
	public static final String ENTERPRISE_STATUS="enterprise_status";
	
	/**通道变量:valid_ivr ivr是否有效*/
	public static final String VALID_IVR="valid_ivr";
	
	/**通道变量:ivr_id */
	public static final String IVR_ID="ivr_id";
	
	/**通道变量:ivr_router_type 路由类型*/
	public static final String IVR_ROUTER_TYPE="ivr_router_type";
	
	/**通道变量:ivr_router_property 路由规则转移的电话*/
	public static final String IVR_ROUTER_TEL="ivr_router_tel";
	/**通道变量:ivr_router_property 路由规则转移的分机*/
	public static final String IVR_ROUTER_EXTEN="ivr_router_exten";
	
	/**通道变量:enterprise_id */
	public static final String ENTERPRISE_ID="enterprise_id";

	/**通道变量:is_ib_record */
	public static final String IS_RECORD = "is_record";

	/**通道变量:is_ob_record  外呼是否录音 0--不录音 ，1--录音 */
	public static final String RECORD_SCOPE = "record_scope";

	/** 号码状态识别功能是否开启 **/
	public static final String IS_CRBT_OPEN = "is_crbt_open";
	/**通道变量:is_ib_record */
	public static final String IS_IB_RECORD = "is_ib_record";
	
	/**通道变量:is_ob_record  外呼是否录音 0--不录音 ，1--录音 */
	public static final String IS_OB_RECORD = "is_ob_record";
	
	/**通道变量:is_restrict_check 是否设置了黑白名单*/
	public static final String IS_RESTRICT_CHECK="is_restrict_check";
	
	/**通道变量:is_own_type 是否支持所属座席或所属队列 0不支持 1:所属座席 2:所属队列*/
	public static final String IS_OWN_TYPE="is_own_type";
	/**通道变量:如果is_own_type=2 呼入的号码属于的队列号*/
	public static final String BELONG_QUEUE = "belong_queue";
	/**通道变量:如果is_own_type=3 呼入的号码属于的座席号*/
	public static final String BELONG_CLIENT = "belong_client";
	
	/**通道变量:cdr_customer_vip 来电是否是VIP*/
	public static final String CDR_CUSTOMER_VIP="cdr_customer_vip";
	
	/**通道变量:cdr_customer_crm_id 来电客户对接crm_id*/
	public static final String CDR_CUSTOMER_CRM_ID="cdr_customer_crm_id";

	/**通道变量:is_call_failed_msg_send 未接来电短信提醒是否打开*/
	public static final String IS_CALL_FAILED_MSG_SEND="is_call_failed_msg_send";
	
	/**通道变量:is_call_success_msg_send 已接来电短信提醒是否打开*/
	public static final String IS_CALL_SUCCESS_MSG_SEND="is_call_success_msg_send";
	
	/**通道变量:is_tail_msg_send 尾巴短信是否打开*/
	public static final String IS_TAIL_MSG_SEND="is_tail_msg_send";
	
	/**通道变量:is_tail_msg_send 尾巴短信是否打开*/
	public static final String OB_SMS_TAIL = "ob_sms_tail";
	
	/**通道变量:is_restrict_tel 是否在黑名单/不在白名单中*/
	public static final String IS_RESTRICT_TEL="is_restrict_tel";
	
	/**通道变量:is_investigation 前台满意度调查时会设置这个变量*/
	public static final String IS_INVESTIGATION = "is_investigation";
	
	/**通道变量:is_investigation_auto 是否自动满意度调查*/
	public static final String IS_INVESTIGATION_AUTO = "is_investigation_auto";
	
	/** 号码状态识别功能是否开启 **/
	public static final String IS_TSI = "is_tel_status_identification";
	
	/**通道变量:is_remember_call 是否开通主叫记忆功能*/
	public static final String IS_REMEMBER_CALL = "is_remember_call";
	
	/**通道变量:cur_queue 当前呼叫的队列*/
	public static final String CUR_QUEUE = "cur_queue";

	public static final String WEBCALL_TEL = "webcall_tel";
	public static final String IS_AMD_ON = "is_amd_on";
	public static final String SUBTEL = "subtel";
	
	/**通道变量:cur_queue 当前呼叫的队列*/
	public static final String NUMBER_TRUNK = "number_trunk";
	public static final String NUMBER_TRUNK_AREA_CODE="number_trunk_area_code";
	/**通道变量:queue_remember_member 上次在此队列中接听这个号码的座席号*/
	public static final String QUEUE_REMEMBER_MEMBER = "queue_remember_member";
	
	/**通道变量:bridged_cno 桥接的座席号*/
	public static final String BRIDGED_CNO = "bridged_cno";
	
	/**通道变量:cno 本通道的座席号*/
	public static final String CNO = "cno";
	public static final String DIAL_TIMEOUT = "dial_timeout";
	/**通道变量:consulter_cno 咨询发起者的座席号*/
	public static final String CONSULTER_CNO = "consulter_cno";
	/**通道变量:consulter_cno 被咨询的座席号*/
	public static final String CONSULTEE_CNO = "consultee_cno";
	
	public static final String CONSULT_THREEWAY_CHAN = "consult_threeway_chan";
	
	public static final String CONSULT_CANCEL = "consult_cancel";
	/**通道变量:consulter_cno 转移发起者的座席号*/
	public static final String TRANSFER_CNO = "transfer_cno";
	/**通道变量:consulter_cno 被转移的座席号*/
	public static final String TRANSFEE_CNO = "transfee_cno";
	public static final String TRANSFER_CHANNEL = "transfer_channel";

	public static final String DISCONNECT_CHAN = "disconnect_chan";
	
	public static final String SPY_CHAN = "spy_chan";
	public static final String SPYER_CNO = "spyer_cno";
	public static final String SPIED_CNO = "spied_cno";
	public static final String SPY_OBJECT = "spy_object";
	public static final String OBJECT_TYPE = "object_type";
	
	public static final String WHISPER_CHAN = "whisper_chan";
	public static final String WHISPER_CNO = "whisper_cno";
	public static final String WHISPERED_CNO = "whispered_cno";
	public static final String WHISPER_OBJECT = "whisper_object";;
	
	public static final String THREEWAY_CHAN = "threeway_chan";
	public static final String THREEWAYER_CNO = "threewayer_cno";
	public static final String THREEWAYED_CNO = "threewayed_cno";
	public static final String THREEWAY_OBJECT = "threeway_object";;
								 
	public static final String BARGE_CHAN = "barge_chan";
	public static final String BARGED_CNO = "barged_cno";
	public static final String BARGER_CNO = "barger_cno";
	public static final String BARGE_OBJECT = "barge_object";
	public static final String BARGER_INTERFACE = "barger_interface";
	
	public static final String PICKUP_CHAN = "pickup_chan";
	public static final String PICKUPER_CNO = "pickuper_cno";
	public static final String PICKUPER_INTERFACE = "pickuper_interface";
	
	public static final String MAIN_CHANNEL = "main_channel";
	
	public static final String TASK_ID = "task_id";
	public static final String CDR_ENTERPRISE_ID = "cdr_enterprise_id";
	public static final String CDR_HOTLINE = "cdr_hotline";
	public static final String CDR_MAIN_UNIQUE_ID = "cdr_main_unique_id";
	public static final String CDR_START_TIME = "cdr_start_time";
	public static final String CDR_DETAIL_CNO = "cdr_detail_cno";
	public static final String CDR_DETAIL_CALL_TYPE="cdr_detail_call_type";
	public static final String CDR_DETAIL_GW_IP = "cdr_detail_gw_ip";
	public static final String CDR_GW_IP = "cdr_gw_ip";
	public static final String CDR_CUSTOMER_NUMBER = "cdr_customer_number";
	public static final String CDR_CUSTOMER_NUMBER_TYPE = "cdr_customer_number_type";
	public static final String CDR_CUSTOMER_AREA_CODE = "cdr_customer_area_code";
	public static final String CDR_NUMBER_TRUNK = "cdr_number_trunk";
	public static final String CDR_CALL_TYPE = "cdr_call_type";
	public static final String CDR_CLIENT_NUMBER = "cdr_client_number";
	public static final String CDR_EXTEN = "cdr_exten";
	public static final String CDR_STATUS = "cdr_status";
	public static final String CDR_BRIDGED_CNO = "cdr_bridged_cno";
	public static final String CDR_TASK_ID = "cdr_task_id";
	public static final String CDR_TASK_INVENTORY_ID = "cdr_task_inventory_id";
	public static final String RECORD_FILE = "record_file";
	public static final String CDR_RECORD_FILE = "cdr_record_file";
	public static final String CDR_IVR_ID = "cdr_ivr_id";
	public static final String CDR_USER_FIELD = "CDR(userfield)";
	public static final String CDR_ORDER_CALL_BACK = "orderCallBackId";
	
	public static final String CUR_NODE = "cur_node";
	public static final String CUR_NODE_ACTION = "cur_node_action";
	
	public static final String CALL_POWER = "call_power";
	
	public static final String PREVIEW_OUTCALL_LEFT_CLID = "preview_outcall_left_clid";
	
	/**内部呼叫座席号*/
	public static final String PREVIEW_OUTCALL_INTERNAL_CALL_CNO = "preview_outcall_internal_call_cno";
	
	/**通道参数:predictive_outcall 对应底层dialplan中的context名称*/
	public static final String PREDICTIVE_OUTCALL="predictive_outcall";
	/**获取座席loginStatus和DeviceStatus的变量*/
	public static final String AGENT_LOGIN_STATUS = "agent_login_status";
	public static final String AGENT_DEVICE_STATUS = "agent_device_status";
	
	public static final String IVR_DB_SQL = "ivr_db_sql";
	public static final String IVR_DB_ENTERPRISE_IVR_ID = "ivr_db_enterprise_ivr_id";
	
	public static final String QUEUE = "queue";
	public static final String QUEUE_FILE = "queue_file";
	public static final String WEBCALL_IVR_ID = "webcall_ivr_id";
	
	public static final String CDR_FORCE_DISCONNECT = "cdr_force_disconnect";
	public static final String RDNIS = "RDNIS";
	
	public static final String OB_DEST_NUMBER= "ob_dest_number";
	
	public static final String IVR_WAIT_STATUS = "IVR_WAIT_STATUS";
	public static final String IVR_WAIT_DONE = "IVR_WAIT_DONE";
	
	public static final String DIRECT_CALL_READ_STATUS = "DIRECT_CALL_READ_STATUS";
	public static final String DIRECT_CALL_READ_DONE = "DIRECT_CALL_READ_DONE";
	
	public static final String WEBCALL_INDEX = "webcall_index";
	public static final String WEBCALL_REQUEST_TIME = "webcall_request_time";
}
