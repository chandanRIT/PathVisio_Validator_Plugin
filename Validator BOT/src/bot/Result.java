package bot;

import org.pathvisio.wikipathways.webservice.WSPathwayInfo;

abstract class Result {
	private WSPathwayInfo pathwayInfo;

	public Result(WSPathwayInfo pathwayInfo) {
		this.pathwayInfo = pathwayInfo;
	}

	public WSPathwayInfo getPathwayInfo() {
		return pathwayInfo;
	}

	/**
	 * Check if the variables in the results equal
	 * those in the tag text.
	 */
	public abstract boolean equalsTag(String tag);
	public abstract String getTagText();
	public abstract boolean shouldTag();
}
