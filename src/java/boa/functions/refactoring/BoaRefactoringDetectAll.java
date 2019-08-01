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

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
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
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;

import boa.datagen.util.FileIO;
import gr.uom.java.xmi.UMLModel;

public class BoaRefactoringDetectAll {

	private static String NAMES_PATH;
	private static String REPOS_PATH;
	private static String OUTPUT_PATH;
	private static Set<RefactoringType> refactoringTypesToConsider = null;

	public static void main(String[] args) {
//		args = new String[] { "/Users/hyj/test3/names.txt", "/Users/hyj/git/BoaData/DataGenInputRepo", "/Users/hyj/test3/output/o.txt" };
		if (args.length < 2) {
			System.err.println("args: NAMES_PATH, REPOS_PATH, OUTPUT_PATH");
		} else {
			NAMES_PATH = args[0];
			String input = FileIO.readFileContents(new File(NAMES_PATH));
			String[] projectNames = input.split("\\r?\\n");
			REPOS_PATH = args[1];
			OUTPUT_PATH = args[2];
			
			List<String> outputs = new ArrayList<String>();
			for (String name : projectNames) {
				
				System.out.println(name + " start");
				
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
						while (i.hasNext()) {
							RevCommit r = i.next();
							List<Refactoring> temp = detect(repo, r);
							
							System.out.println(name + " " + r.getName() + " detected " + temp.size());
							
							for (Refactoring rf : temp) {
								String output = name + " " + r.getName() + "=" + rf.toString();  
								outputs.add(output);
							}
						}
						revWalk.dispose();
						repo.close();
					} catch (IOException e) {
						e.printStackTrace();
						continue;
					}
				} else {
					System.out.println(name + " not exist! Continue");
				}
			}
			writeOutputs(outputs, OUTPUT_PATH);

		}
	}

	public static void writeOutputs(List<String> outputs, String path) {
		StringBuilder sb = new StringBuilder();
		for (String s : outputs)
			sb.append(s+"\n");
		FileIO.writeFileContents(new File(path), sb.toString());
	}

	private static List<Refactoring> detect(Repository repo, RevCommit next) {
		try {
			return detectRefactorings(repo, next);
		} catch (Exception e) {
			return new ArrayList<Refactoring>();
		}
	}

	protected static List<Refactoring> detectRefactorings(Repository repository, RevCommit currentCommit)
			throws Exception {
		List<Refactoring> refactoringsAtRevision;
		List<String> filePathsBefore = new ArrayList<String>();
		List<String> filePathsCurrent = new ArrayList<String>();
		Map<String, String> renamedFilesHint = new HashMap<String, String>();
		fileTreeDiff(repository, currentCommit, filePathsBefore, filePathsCurrent, renamedFilesHint);

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
				refactoringsAtRevision = filter(refactoringsAtRevision);
			} else {
				refactoringsAtRevision = Collections.emptyList();
			}
			walk.dispose();
		}
		return refactoringsAtRevision;
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

	public static void setRefactoringTypesToConsider(RefactoringType... types) {
		refactoringTypesToConsider = new HashSet<RefactoringType>();
		for (RefactoringType type : types) {
			refactoringTypesToConsider.add(type);
		}
	}

	protected static List<Refactoring> filter(List<Refactoring> refactoringsAtRevision) {
		if (refactoringTypesToConsider == null) {
			return refactoringsAtRevision;
		}
		List<Refactoring> filteredList = new ArrayList<Refactoring>();
		for (Refactoring ref : refactoringsAtRevision) {
			if (refactoringTypesToConsider.contains(ref.getRefactoringType())) {
				filteredList.add(ref);
			}
		}
		return filteredList;
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

	private static void fileTreeDiff(Repository repository, RevCommit currentCommit, List<String> javaFilesBefore,
			List<String> javaFilesCurrent, Map<String, String> renamedFilesHint) throws Exception {
		if (currentCommit.getParentCount() > 0) {
			ObjectId oldTree = currentCommit.getParent(0).getTree();
			ObjectId newTree = currentCommit.getTree();
			final TreeWalk tw = new TreeWalk(repository);
			tw.setRecursive(true);
			tw.addTree(oldTree);
			tw.addTree(newTree);

			final RenameDetector rd = new RenameDetector(repository);
			rd.setRenameScore(80);
			rd.addAll(DiffEntry.scan(tw));

			for (DiffEntry diff : rd.compute(tw.getObjectReader(), null)) {
				ChangeType changeType = diff.getChangeType();
				String oldPath = diff.getOldPath();
				String newPath = diff.getNewPath();
				if (changeType != ChangeType.ADD) {
					if (isJavafile(oldPath)) {
						javaFilesBefore.add(oldPath);
					}
				}
				if (changeType != ChangeType.DELETE) {
					if (isJavafile(newPath)) {
						javaFilesCurrent.add(newPath);
					}
				}
				if (changeType == ChangeType.RENAME && diff.getScore() >= rd.getRenameScore()) {
					if (isJavafile(oldPath) && isJavafile(newPath)) {
						renamedFilesHint.put(oldPath, newPath);
					}
				}
			}
		}
	}

	private static boolean isJavafile(String path) {
		return path.endsWith(".java");
	}

}
