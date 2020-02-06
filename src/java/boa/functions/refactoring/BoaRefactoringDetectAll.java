package boa.functions.refactoring;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

import boa.datagen.util.FileIO;
import gr.uom.java.xmi.UMLModel;

public class BoaRefactoringDetectAll {

	private static String NAMES_PATH;
	private static String REPOS_PATH;
	private static String OUTPUT_PATH;
	private static int TIME_OUT; // in seconds
	private static String ID;

	private static ExecutorService executor;
	private static List<String> processedProjects = new ArrayList<String>();
	private static List<String> exceptions = new ArrayList<String>();
	private static int TIME_OUT_COUNT = 0;

	public static void main(String[] args) {
//		args = new String[] { "/Users/hyj/test6/names.txt", "/Users/hyj/git/BoaData/DataGenInputRepo",
//				"/Users/hyj/test6/output", "1", "2" };
		if (args.length < 4) {
			System.err.println("args: NAMES_PATH, REPOS_PATH, OUTPUT_PATH, TIME_OUT");
		} else {
			NAMES_PATH = args[0];
			String input = FileIO.readFileContents(new File(NAMES_PATH));
			String[] projectNames = input.split("\\r?\\n");
			REPOS_PATH = args[1];
			OUTPUT_PATH = args[2];
			TIME_OUT = Integer.parseInt(args[3]); // in seconds
			ID = args[4];

			HashSet<String> typeSet = BoaRefactoringIntrinsics.getConsideredTypes();
			
			StringBuilder sb = new StringBuilder();
			int projectCount = 0;
			int totolRefCommit = 0;
			startJSON(sb);
			for (String name : projectNames) {
				projectCount++;
				TIME_OUT_COUNT = 0;
				boolean ignore = false;
				System.err.println(projectCount + "th project " + name + " start");
				
				// one project start
				StringBuilder psb = new StringBuilder();
				File gitDir = new File(REPOS_PATH + "/" + name + "/.git");
				if (gitDir.exists()) {

					try {
						Repository repo = new FileRepositoryBuilder().setGitDir(gitDir).build();
						RevWalk revWalk = new RevWalk(repo);
						Set<RevCommit> heads = getHeads(revWalk, repo);

						revWalk.markStart(heads);
						revWalk.sort(RevSort.TOPO, true);
						revWalk.sort(RevSort.COMMIT_TIME_DESC, true);
						revWalk.sort(RevSort.REVERSE, true);

						Iterator<RevCommit> i = revWalk.iterator();

						int commitCount = 0;
						while (i.hasNext()) {
							if (TIME_OUT_COUNT > 10) {
								ignore = true;
								break;
							}
							RevCommit r = i.next();
							commitCount++;
							System.out.println(name + " " + commitCount + "th Commit " + r.getName() + " started");
							try {

								long before = System.currentTimeMillis();
								List<Refactoring> temp = getRefactorings(TIME_OUT * 1000, name, repo, r);
								temp = filterTypes(temp, typeSet);
								if (temp.size() > 0) {
									long after = System.currentTimeMillis();
									totolRefCommit++;
									System.out.println(
											name + " " + commitCount + "th Commit " + r.getName() + " detected "
													+ temp.size() + " with time secs: " + (after - before) / 1000.0);
									if(totolRefCommit > 1) {
										psb.append(",").append("\n");
									}
									commitJSON(psb, name, r.getName(), temp);
								}

							} catch (OutOfMemoryError e) {
								System.err.println(commitCount + "th Commit " + r.getName() + " OutOfMemoryError");
								continue;
							} finally {
								System.gc();
							}

						}
						revWalk.dispose();
						repo.close();
						
					} catch (Throwable e) {
						e.printStackTrace();
						continue;
					}  finally {
						System.gc();
					}
					System.err.println(projectCount + "th project " + name + " end");
					if (!ignore) {
						processedProjects.add(name);
						sb.append(psb);
					} else {
						System.err.println(projectCount + "th project " + name + " is ignored");
					}
				} else {
					System.err.println(projectCount + "th project " + name + " not exist! Continue");
				}
				// one project end
			}
			endJSON(sb);
			System.err.println("Write start");
			FileIO.writeFileContents(new File(OUTPUT_PATH + "/o" + ID + ".json"), sb.toString());
			writeOutputs(processedProjects, OUTPUT_PATH + "/processed_" + ID + ".txt");
			writeOutputs(exceptions, OUTPUT_PATH + "/excepted_" + ID + ".txt");
			System.err.println("Write end");
			
			if (executor != null)
				executor.shutdown();
			System.exit(1);
		}
	}

	private static List<Refactoring> getRefactorings(int timeout, String projectName, Repository repo, RevCommit r) {
		executor = Executors.newSingleThreadExecutor();
		Future<List<Refactoring>> future = executor.submit(() -> {
			try {
				return detectRefactorings(repo, r);
			} catch (Throwable e) {
				System.err.println(projectName + " " + r.getName() + " Detect Throwable");
				TIME_OUT_COUNT++;
				exceptions.add(projectName + " " + r.getName());
				return new ArrayList<Refactoring>();
			}
		});

		try {
			List<Refactoring> results = future.get(timeout, TimeUnit.MILLISECONDS);
			return results;
		} catch (TimeoutException e) {
			System.err.println(projectName + " " + r.getName() + " TimeoutException " + timeout / 1000.0 + " sec");
			TIME_OUT_COUNT++;
			exceptions.add(projectName + " " + r.getName());
		} catch (InterruptedException e) {
			System.err.println(projectName + " " + r.getName() + " InterruptedException");
			TIME_OUT_COUNT++;
			exceptions.add(projectName + " " + r.getName());
		} catch (ExecutionException e) {
			System.err.println(projectName + " " + r.getName() + " ExecutionException");
			TIME_OUT_COUNT++;
			exceptions.add(projectName + " " + r.getName());
		} finally {
			executor.shutdown();
			executor = null;
		}
		return new ArrayList<Refactoring>();
	}

	private static List<Refactoring> filterTypes(List<Refactoring> temp, HashSet<String> typeSet) {
		ArrayList<Refactoring> res = new ArrayList<Refactoring>();
		for (Refactoring ref : temp) {
			if (typeSet.contains(ref.getName()))
				res.add(ref);
		}
		return res;
	}

	public static void writeOutputs(List<String> outputs, String path) {
		StringBuilder sb = new StringBuilder();
		for (String s : outputs)
			sb.append(s + "\n");
		FileIO.writeFileContents(new File(path), sb.toString());
	}

	protected static List<Refactoring> detectRefactorings(Repository repository, RevCommit currentCommit)
			throws Exception {
		GitService gitService = new GitServiceImpl();
		List<Refactoring> refactoringsAtRevision;
		List<String> filePathsBefore = new ArrayList<String>();
		List<String> filePathsCurrent = new ArrayList<String>();
		Map<String, String> renamedFilesHint = new HashMap<String, String>();
		gitService.fileTreeDiff(repository, currentCommit, filePathsBefore, filePathsCurrent, renamedFilesHint);

		Set<String> repositoryDirectoriesBefore = new LinkedHashSet<String>();
		Set<String> repositoryDirectoriesCurrent = new LinkedHashSet<String>();
		Map<String, String> fileContentsBefore = new LinkedHashMap<String, String>();
		Map<String, String> fileContentsCurrent = new LinkedHashMap<String, String>();
		try (RevWalk walk = new RevWalk(repository)) {
			
			if (!filePathsBefore.isEmpty() && !filePathsCurrent.isEmpty() && currentCommit.getParentCount() == 1) {
				RevCommit parentCommit = currentCommit.getParent(0);
				populateFileContents(repository, parentCommit, filePathsBefore, fileContentsBefore,
						repositoryDirectoriesBefore);
				UMLModel parentUMLModel = GitHistoryRefactoringMinerImpl.createModel(fileContentsBefore,
						repositoryDirectoriesBefore);

				populateFileContents(repository, currentCommit, filePathsCurrent, fileContentsCurrent,
						repositoryDirectoriesCurrent);
				UMLModel currentUMLModel = GitHistoryRefactoringMinerImpl.createModel(fileContentsCurrent,
						repositoryDirectoriesCurrent);

				refactoringsAtRevision = parentUMLModel.diff(currentUMLModel, renamedFilesHint).getRefactorings();
				refactoringsAtRevision = removeDuplicates(refactoringsAtRevision);
//				refactoringsAtRevision = filter(refactoringsAtRevision);
			} else {
				refactoringsAtRevision = Collections.emptyList();
			}
			walk.dispose();
		}
		return refactoringsAtRevision;
	}

	private static List<Refactoring> removeDuplicates(List<Refactoring> refactoringsAtRevision) {
		ArrayList<Refactoring> res = new ArrayList<Refactoring>();
		HashSet<String> set = new HashSet<String>();
		for (Refactoring ref : refactoringsAtRevision) {
			String description = ref.toString();
			if (!set.contains(description)) {
				res.add(ref);
				set.add(description);
			}
		}
		return res;
	}

	private static void populateFileContents(Repository repository, RevCommit commit, List<String> filePaths,
			Map<String, String> fileContents, Set<String> repositoryDirectories) throws Exception {
		RevTree parentTree = commit.getTree();
		try (TreeWalk treeWalk = new TreeWalk(repository)) {
			treeWalk.addTree(parentTree);
			treeWalk.setRecursive(true);
			while (treeWalk.next()) {
				String pathString = treeWalk.getPathString();
				if (filePaths.contains(pathString)) {
					ObjectId objectId = treeWalk.getObjectId(0);
					ObjectLoader loader = repository.open(objectId);
					StringWriter writer = new StringWriter();
					IOUtils.copy(loader.openStream(), writer);
					fileContents.put(pathString, writer.toString());
				}
				if (pathString.endsWith(".java")) {
					int idx = pathString.lastIndexOf("/");
					if (idx != -1) {
						String directory = pathString.substring(0, pathString.lastIndexOf("/"));
						repositoryDirectories.add(directory);
						// include sub-directories
						String subDirectory = new String(directory);
						while (subDirectory.contains("/")) {
							subDirectory = subDirectory.substring(0, subDirectory.lastIndexOf("/"));
							repositoryDirectories.add(subDirectory);
						}
					}
				}
			}
		}
	}

	private static Set<RevCommit> getHeads(RevWalk revWalk, Repository repo) {
		Git git = new Git(repo);
		Set<RevCommit> heads = new HashSet<RevCommit>();
		try {
			for (final Ref ref : git.branchList().call()) {
				heads.add(revWalk.parseCommit(repo.resolve(ref.getName())));
			}
		} catch (final GitAPIException e) {
			System.err.println("Git Error reading heads: " + e.getMessage());
		} catch (final IOException e) {
			System.err.println("Git Error reading heads: " + e.getMessage());
		} finally {
			git.close();
		}
		return heads;
	}
	
	private static void startJSON(StringBuilder sb) {
		sb.append("{").append("\n");
		sb.append("\"").append("commits").append("\"").append(": ");
		sb.append("[").append("\n");
	}

	private static void endJSON(StringBuilder sb) {
		sb.append("]").append("\n");
		sb.append("}");
	}
	
	private static void commitJSON(StringBuilder sb, String projectName, String currentCommitId, List<Refactoring> refactoringsAtRevision) {
		sb.append("{").append("\n");
		sb.append("\t").append("\"").append("project_name").append("\"").append(": ").append("\"").append(projectName).append("\"").append(",").append("\n");
		sb.append("\t").append("\"").append("sha1").append("\"").append(": ").append("\"").append(currentCommitId).append("\"").append(",").append("\n");
		sb.append("\t").append("\"").append("refactorings").append("\"").append(": ");
		sb.append("[");
		int counter = 0;
		for(Refactoring refactoring : refactoringsAtRevision) {
			sb.append(refactoring.toJSON());
			if(counter < refactoringsAtRevision.size()-1) {
				sb.append(",");
			}
			sb.append("\n");
			counter++;
		}
		sb.append("]").append("\n");
		sb.append("}");
	}

}
