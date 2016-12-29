/*
 * Copyright 2015, Hridesh Rajan, Robert Dyer, Hoan Nguyen
 *                 and Iowa State University of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boa.datagen;

/**
 * @author rdyer
 */
public class DefaultProperties {
	public static final boolean DEBUG = false;
	public static final String NUM_THREADS = "1";
	public static final String MAX_COMMITS = "10000";

	// HBase tables
	public static final String HBASE_PROJECTS_TABLE = "projects";
	public static final String HBASE_AST_TABLE = "ast";
	public static final String HBASE_COMMENTS_TABLE = "comments";
	public static final String HBASE_ISSUES_TABLE = "issues";
	public static final String HBASE_LOC_TABLE = "loc";

	public static final String HBASE_PROJECTS_COL = "p";
	public static final String HBASE_AST_COL = "a";
	public static final String HBASE_COMMENTS_COL = "c";
	public static final String HBASE_ISSUES_COL = "i";
	public static final String HBASE_LOC_COL = "l";

	public static final String HBASE_DELIMITER = "!!";

	// Sequence file paths
	public static final String SEQ_PROJECTS_PATH = "projects.seq";
	public static final String SEQ_AST_DIR = "ast";
	public static final String SEQ_AST_PATH = "data";
	public static final String SEQ_COMMENTS_DIR = "comments";
	public static final String SEQ_COMMENTS_PATH = "data";
	public static final String SEQ_ISSUES_DIR = "issues";
	public static final String SEQ_ISSUES_PATH = "data";

	// SF.net paths
	public static final String SF_JSON_PATH = "json";
	public static final String SF_JSON_CACHE_PATH = "json_cache";
	public static final String SF_SVN_PATH = "svn";
	public static final String SF_TICKETS_PATH = "tickets";
	
	// GitHub paths
	public static  String GH_JSON_PATH = "repos-metadata-Boa-upto1213";
	public static  String GH_JSON_CACHE_PATH = "json_cache";
//	public static final String GH_GIT_PATH = "github/repositories";
	public static String GH_GIT_PATH = "/Users/nmtiwari/nmt/githubCacheJSon/github";
	public static final String GH_TICKETS_PATH = "tickets";
}
