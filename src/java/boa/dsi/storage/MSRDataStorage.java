package boa.dsi.storage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

import com.google.protobuf.GeneratedMessage;

import boa.dsi.DSIProperties;
import boa.dsi.dsource.AbstractSource;
import boa.dsi.storage.sequencefile.SequenceFileStorage;
import boa.types.Ast.ASTRoot;
import boa.types.Code.CodeRepository;
import boa.types.Code.Revision;
import boa.types.Diff.ChangedFile;
import boa.types.Toplevel.Project;

public class MSRDataStorage extends SequenceFileStorage {
	protected SequenceFile.Writer astFileWriter;

	public MSRDataStorage(AbstractSource parser) {
		super(parser);
	}

	public MSRDataStorage(String location, AbstractSource parser) {
		super(location, parser);
	}

	@Override
	public void store(List<GeneratedMessage> dataInstance) {
		this.openWriter(DSIProperties.HADOOP_SEQ_FILE_LOCATION);

		for (GeneratedMessage data : dataInstance) {
			Project p = (Project) data;
			HashMap<String, ASTRoot> filesAST = new HashMap<String, ASTRoot>();
			Project.Builder pb = Project.newBuilder();
			this.processBoaProject(p, pb, filesAST);
			try {
				this.seqFileWriter.append(new Text("data1"), new BytesWritable(pb.build().toByteArray()));
			} catch (IOException e) {
				e.printStackTrace();
			}

			for (String key : filesAST.keySet()) {
				try {
					this.astFileWriter.append(new Text(key), new BytesWritable(filesAST.get(key).toByteArray()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		this.closeWrite();
	}

	@Override
	protected boolean openWriter(String seqPath) {
		super.openWriter(seqPath + "/" + DSIProperties.HADOOP_SEQ_FILE_NAME);
		FileSystem fileSystem;
		try {
			fileSystem = FileSystem.get(conf);
			this.astFileWriter = SequenceFile.createWriter(fileSystem, conf,
					new Path(seqPath + "/" + DSIProperties.HADOOP_AST_FILE_NAME), Text.class, BytesWritable.class);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	protected boolean closeWrite() {
		super.closeWrite();
		try {
			astFileWriter.close();
			this.genMapFile(DSIProperties.HADOOP_SEQ_FILE_LOCATION);
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	private Project processBoaProject(Project p, Project.Builder pb, HashMap<String, ASTRoot> filesAST) {
		pb.setHomepageUrl(p.getHomepageUrl());
		pb.setId(p.getId());
		pb.setName(p.getName());
		pb.setProjectUrl(p.getProjectUrl());
		pb.setKind(p.getKind());

		pb.addAllAudiences(p.getAudiencesList());
		pb.addAllDatabases(p.getDatabasesList());
		pb.addAllDevelopers(p.getDevelopersList());
		pb.addAllOperatingSystems(p.getOperatingSystemsList());
		pb.addAllProgrammingLanguages(p.getProgrammingLanguagesList());
		pb.addAllLicenses(p.getLicensesList());
		pb.addAllInterfaces(p.getInterfacesList());
		pb.addAllTopics(p.getTopicsList());
		pb.addAllStatus(p.getStatusList());
		pb.addAllTranslations(p.getTranslationsList());
		pb.addAllMaintainers(p.getMaintainersList());
		pb.addAllIssueRepositories(p.getIssueRepositoriesList());

		if (p.hasCreatedDate()) {
			pb.setCreatedDate(p.getCreatedDate());
		}
		if (p.hasDonations()) {
			pb.setDonations(p.getDonations());
		}
		if (p.hasDescription()) {
			pb.setDescription(p.getDescription());
		}

		for (CodeRepository code : p.getCodeRepositoriesList()) {
			CodeRepository.Builder cb = CodeRepository.newBuilder();
			cb.setUrl(code.getUrl());
			cb.setKind(code.getKind());
			cb.addAllBranches(code.getBranchesList());
			cb.addAllBranchNames(code.getBranchNamesList());
			cb.addAllTagNames(code.getTagNamesList());
			cb.addAllTags(code.getTagsList());
			for (Revision r : code.getRevisionsList()) {
				Revision.Builder rb = Revision.newBuilder();
				rb.setAuthor(r.getAuthor());
				rb.setId(r.getId());
				rb.setCommitDate(r.getCommitDate());
				rb.setCommitter(r.getCommitter());
				rb.setLog(r.getLog());
				rb.addAllChildren(r.getChildrenList());
				rb.addAllParents(r.getParentsList());

				for (ChangedFile f : r.getFilesList()) {
					ChangedFile.Builder fb = ChangedFile.newBuilder();
					fb.setKind(f.getKind());
					fb.setChange(f.getChange());
					fb.setName(f.getName());
					fb.setKey(f.getKey());

					fb.addAllChanges(f.getChangesList());
					fb.addAllPreviousIndex(f.getPreviousIndexList());
					fb.addAllPreviousVersions(f.getPreviousVersionsList());

					if (f.hasAst()) {
						filesAST.put(f.getKey(), f.getAst());
					}

					if (f.hasComments()) {
						fb.setComments(f.getComments());
					}
					rb.addFiles(fb.build());
				}
				cb.addRevisions(rb.build());
			}
			pb.addCodeRepositories(cb.build());
		}
		return pb.build();

	}

	public void genMapFile(String filepath) throws Exception {
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);
		Path path = new Path(filepath + "/" + DSIProperties.HADOOP_AST_FILE_NAME);
		SequenceFile.Sorter sort = new SequenceFile.Sorter(fs, Text.class, BytesWritable.class, conf);
		Path[] input = { path };
		path = new Path(filepath + "/data");
		String name = path.getName();
		sort.sort(input, path, false);
		if (fs.isFile(path)) {
			if (path.getName().equals(MapFile.DATA_FILE_NAME)) {
				MapFile.fix(fs, path.getParent(), Text.class, BytesWritable.class, false, conf);
			} else {
				Path dataFile = new Path(path.getParent(), MapFile.DATA_FILE_NAME);
				fs.rename(path, dataFile);
				Path dir = new Path(path.getParent(), name);
				fs.mkdirs(dir);
				fs.rename(dataFile, new Path(dir, dataFile.getName()));
				MapFile.fix(fs, dir, Text.class, BytesWritable.class, false, conf);
			}
		} else {
			FileStatus[] files = fs.listStatus(path);
			for (FileStatus file : files) {
				path = file.getPath();
				if (fs.isFile(path)) {
					Path dataFile = new Path(path.getParent(), MapFile.DATA_FILE_NAME);
					fs.rename(path, dataFile);
					MapFile.fix(fs, dataFile.getParent(), Text.class, BytesWritable.class, false, conf);
					break;
				}
			}
		}
		File ast = new File(filepath + "/ast.seq");
		if (ast.exists()) {
			ast.delete();
		}
		fs.close();
	}
}
