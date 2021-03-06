package bot;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.Properties;

import org.pathvisio.debug.Logger;
import org.pathvisio.model.Pathway;
import org.pathvisio.model.PathwayElement.Comment;
import org.pathvisio.wikipathways.WikiPathways;
import org.pathvisio.wikipathways.bots.Bot;
import org.pathvisio.wikipathways.bots.BotReport;
import org.pathvisio.wikipathways.webservice.WSPathwayInfo;

public class DescriptionBot extends Bot {
	private static final String CURATIONTAG = "Curation:MissingDescription";

	public DescriptionBot(Properties props) throws BotException {
		super(props);
	}

	
	public String getTagName() {
		return CURATIONTAG;
	}
	
	public BotReport createReport(Collection<Result> result) {
		BotReport report = new BotReport(
				new String[] { "Has description" }
		);
		report.setTitle("DescriptionBot scan report");
		report.setDescription("Checks pathways for an empty description field");

		int nrMissing = 0;
		for(Result r : result) {
			DescriptionResult dr = (DescriptionResult)r;
			if(!dr.hasDescription) nrMissing++;
			report.setRow(dr.getPathwayInfo(), new String[] { dr.hasDescription + "" });
		}
		report.setComment("Number of pathways", "" + result.size());
		report.setComment("Number of pathways missing description", "" + nrMissing);
		return report;
	}


	protected Result scanPathway(File pathwayFile) throws BotException {
		try {
			Pathway p = new Pathway();
			p.readFromXml(pathwayFile, true);

			String comment = null;
			for(Comment c : p.getMappInfo().getComments()) {
				if(WikiPathways.COMMENT_DESCRIPTION.equals(c.getSource())) {
					comment = c.getComment();
					break;
				}
			}
			boolean hasDescr = comment != null && !"".equals(comment);
			WSPathwayInfo wsp = getCache().getPathwayInfo(pathwayFile);
			return new DescriptionResult(wsp, hasDescr);
		} catch(Exception e) {
			throw new BotException(e);
		}
	}

	public static void main(String[] args) {
		try {
			Logger.log.trace("Starting DescriptionBot");
			Properties props = new Properties();
			props.load(new FileInputStream(new File(args[0])));
			DescriptionBot bot = new DescriptionBot(props);
			Bot.runAll(bot, new File(args[1] + ".html"), new File(args[1] + ".txt"));
		} catch(Exception e) {
			e.printStackTrace();
			printUsage();
		}
	}

	static private void printUsage() {
		System.out.println(
			"Usage:\n" +
			"java org.pathvisio.wikipathways.bots.DescriptionBot propsfile reportfilename\n" +
			"Where:\n" +
			"-propsfile: a properties file containing the bot properties\n" +
			"-reportfilename: the base name of the file that will be used to write reports to " +
			"(extension will be added automatically)\n"
		);
	}

	class DescriptionResult extends Result {
		boolean hasDescription;

		public DescriptionResult(WSPathwayInfo pathwayInfo, boolean hasDescription) {
			super(pathwayInfo);
			this.hasDescription = hasDescription;
		}

		public boolean equalsTag(String tag) {
			return getTagText().equals(tag);
		}

		public String getTagText() {
			return "";
		}

		public boolean shouldTag() {
			return !hasDescription;
		}
	}
}

