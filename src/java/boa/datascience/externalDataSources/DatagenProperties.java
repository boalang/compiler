package boa.datascience.externalDataSources;

public class DatagenProperties {
	public static final boolean DEBUG = false;
	public static final String NUM_THREADS = "1";
	public static final String SEQ_PROJECTS_NAME = "projects.seq";
	public static final String SEQ_AST_NAME = "data";
	public static final String BUF_MAP = "buf-map";

	public static String CANDOIA_PATH = "./../";
	public static String CANDOIA_OUTPUT_PATH = "./../";
	public static String CANDOIA_TRASH_PATH = "./../";
	public static final String CANDOIA_DIR_NAME = "candoia_raw_data";
	public static final String CANDOIA_SOURCE_DIR_NAME = "repos";
	public static final String CANDOIA_JSON_DIR_NAME = "json";
	public static final String CANDOIA_ISSUE_DIR_NAME = "issues";
	public static final String CANDOIA_SOURCE_DIR_PATH = CANDOIA_PATH + CANDOIA_DIR_NAME + "/"
			+ CANDOIA_SOURCE_DIR_NAME;
	public static final String CANDOIA_SOURCE_JSON_PATH = CANDOIA_PATH + CANDOIA_DIR_NAME + "/" + CANDOIA_JSON_DIR_NAME
			+ "/" + CANDOIA_SOURCE_DIR_NAME;
	public static final String CANDOIA_ISSUE_JSON_PATH = CANDOIA_PATH + CANDOIA_DIR_NAME + "/" + CANDOIA_JSON_DIR_NAME
			+ "/" + CANDOIA_ISSUE_DIR_NAME;

	public static final String PARSE = "p";
	public static final String COMPILE = "c";
	public static final String EXECUTE = "e";
	public static final String GENERATE = "g";
	public static final String OUTPUT = "o";
	public static final String INPUT = "in";
	public static final String TRASH = "trash";
	public static final String TEMP_TRASH_WORK = TRASH + "/CANDOIA_TEMP_DATA";

	public static final String SETTINGS_JSON_FILE_PATH = "/src/java/boa/datagen/settings/";
	public static final String SETTINGS_JSON_FILE_NAME = "settings.json";
	public static final String DATAREADER_FIELD_IN_JSON = "datareaders";
	public static final int DATAREADER_FIELD_INDEX_JSON = 0;

	public static final String HADOOP_SEQ_FILE_LOCATION = CANDOIA_OUTPUT_PATH;
	public static final String HADOOP_SEQ_FILE_NAME = "projects.seq";

	public static final String BOA_OUTPUT_DIR_NAME = "boaOutput";
	public static String BOA_OUTPUT_DIR_PATH = "./../";
	public static String  BOA_OUT = DatagenProperties.BOA_OUTPUT_DIR_PATH + "/" + DatagenProperties.BOA_OUTPUT_DIR_NAME;

}