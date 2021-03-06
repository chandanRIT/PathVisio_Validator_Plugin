/**
 * The information, opinions, data, and statements contained herein are not 
 * necessarily those of the U.S. Government or the National Institutes of Health (NIH) 
 * and should not be interpreted, acted on, or represented as such.
 * 
 * Reference herein to any specific commercial product, process, or service by 
 * trade name, trademark, manufacturer, or otherwise, does not necessarily 
 * constitute or imply its endorsement, recommendation, or favoring by the U.S. 
 * Government, NIH, or any of their employees and contractors.
 * 
 * The U.S. Government, NIH and their employees and contractors do not make 
 * any warranty, express or implied, including the warranties of merchantability 
 * and fitness for a particular purpose with respect to this document. In addition, 
 * the U.S. Government, NIH, and their employees and contractors assume no legal 
 * liability for the accuracy, completeness, or usefulness of any information, 
 * apparatus, product, or process disclosed herein and do not represent that use 
 * of such information, apparatus, product or process would not infringe on 
 * privately owned rights.
 * 
 * This document is sponsored by the NIH, along with private companies and other 
 * organizations. Accordingly, other parties may retain all rights to publish or 
 * reproduce these documents or to allow others to do so. This document may be 
 * protected under the U.S. and foreign Copyright laws. 
 * 
 * Permission to reproduce may be required.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 */
package gov.nih.nci.lmp.mimGpml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import gov.nih.nci.lmp.mim.mimVisLevel1.*;

import org.apache.xmlbeans.*;

import org.bridgedb.DataSource;

import org.apache.commons.collections.BidiMap;

import org.pathvisio.core.debug.Logger;
import org.pathvisio.core.model.AnchorType;
import org.pathvisio.core.model.ConnectorType;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.DataNodeType;
import org.pathvisio.core.model.GroupStyle;
import org.pathvisio.core.model.LineType;
import org.pathvisio.core.model.MLine;
import org.pathvisio.core.model.ObjectType;
import org.pathvisio.core.model.Pathway;
import org.pathvisio.core.model.PathwayElement;
import org.pathvisio.core.model.ShapeType;
import org.pathvisio.core.model.PathwayElement.Comment;
import org.pathvisio.core.model.PathwayElement.MAnchor;
import org.pathvisio.core.model.PathwayElement.MPoint;
import org.pathvisio.core.biopax.BiopaxReferenceManager;
import org.pathvisio.core.biopax.BiopaxElement;
import org.pathvisio.core.biopax.PublicationXref;
import org.pathvisio.core.util.FileUtils;
import org.pathvisio.core.view.ShapeRegistry;

/**
 * Class for the import of MIMML.
 * 
 * @author Augustin Luna <augustin@mail.nih.gov>
 * @author Margot Sunshine
 * 
 * @version 1.0
 * @since 1.0
 * 
 */
public class ImporterHelper extends CommonHelper {

	/** The imported file. */
	private File file;

	/** Visual diagram document. */
	private DiagramDocument visDoc;

	/** The diagram. */
	private DiagramType dia;

	/** The Pathvisio pathway. */
	private Pathway pw;

	/**
	 * Instantiates a new importer helper.
	 */
	public ImporterHelper(File file) throws ConverterException {
		this.file = file;
		this.pw = new Pathway();

		mapDiagram();
	}

	/**
	 * Parses the diagram XML.
	 * 
	 * @param xmlfile
	 *            the xmlfile
	 */
	public void parseDiagramXml(File xmlFile) throws XmlException, IOException {

		Logger.log.trace("Entering parseDiagramXml");

		try {

			visDoc = DiagramDocument.Factory.parse(xmlFile);
			Logger.log.debug("parseDiagramXml valid?: " + visDoc.validate());

			dia = visDoc.getDiagram();
			// System.out.println("\n\nVisDoc: ");
			// visDoc.save(System.out, getXmlOptions());
		} catch (XmlException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Process entity glyph.
	 */
	private void processEntityGlyph() {
		List<EntityGlyphType> entGlyphTypeList = this.visDoc.getDiagram()
				.getEntityGlyphList();

		for (EntityGlyphType entGlyphType : entGlyphTypeList) {
			System.out.println("Entity: " + entGlyphType.getDisplayName());
		}
	}

	/**
	 * Recalculate lines. Helps to adjust anchors on lines by clearing out
	 * cache.
	 * 
	 * @param pathway
	 *            the pathway
	 * @throws ConverterException
	 *             the converter exception
	 */
	private static void recalculateLines(Pathway pathway)
			throws ConverterException {

		for (PathwayElement pe : pathway.getDataObjects()) {
			if (pe.getObjectType() == ObjectType.LINE) {
				((MLine) pe).getConnectorShape().recalculateShape(((MLine) pe));
			}
		}
	}

	/**
	 * Gets the pathway.
	 * 
	 * @return the pw
	 */
	public Pathway getPw() throws ConverterException {
		recalculateLines(pw);

		// Taken from Pathway.readFromXml()
		// pw.setSourceFile(file);
		pw.clearChangedFlag();

		return pw;
	}

	/**
	 * Sets the pathway.
	 * 
	 * @param pw
	 *            the new pw
	 */
	public void setPw(Pathway pw) {
		this.pw = pw;
	}

	/**
	 * Sets the MIM-Vis doc.
	 * 
	 * @param visDoc
	 *            the new vis doc
	 */
	public void setVisDoc(DiagramDocument visDoc) {
		this.visDoc = visDoc;
	}

	/**
	 * Gets the MIM-Vis doc.
	 * 
	 * @return the vis doc
	 */
	public DiagramDocument getVisDoc() {
		return this.visDoc;
	}

	/**
	 * Map diagram.
	 */
	private Pathway mapDiagram() throws ConverterException {

		Logger.log.info("Parse diagram");

		try {
			parseDiagramXml(this.file);

			// TODO: Add Junit test for invalid MIMML files
			if (!validateXml(visDoc)) {
				throw new ConverterException("Invalid MIMML file.");
			}
		} catch (XmlException e) {
			Logger.log.error(e.getMessage());
		} catch (IOException e) {
			Logger.log.error(e.getMessage());
		}

		// Add element that contains BioPAX
		PathwayElement biopax = PathwayElement
				.createPathwayElement(ObjectType.BIOPAX);
		pw.add(biopax);

		PathwayElement info = PathwayElement
				.createPathwayElement(ObjectType.INFOBOX);
		pw.add(info);

		info.setMHeight(dia.getHeight());
		info.setMWidth(dia.getWidth());

		// Check for the case where there is no MIMBio element
		if (dia.isSetMimBio()) {
			if (dia.getMimBio().isSetTitle()) {
				pw.getMappInfo().setMapInfoName(dia.getMimBio().getTitle());
			}

			if (dia.getMimBio().isSetIdentifier()) {
				pw.getMappInfo().setVersion(dia.getMimBio().getIdentifier());
			}

			if (dia.getMimBio().isSetRights()) {
				pw.getMappInfo().setCopyright(dia.getMimBio().getRights());
			}

			if (dia.getMimBio().isSetSource()) {
				pw.getMappInfo().setMapInfoDataSource(
						dia.getMimBio().getSource());
			}

			if (dia.getMimBio().isSetDescription()) {
				pw.getMappInfo().addComment(dia.getMimBio().getDescription(),
						"");
			}

			if (dia.getMimBio().sizeOfCreatorArray() > 0) {
				String str = "";

				for (String s : dia.getMimBio().getCreatorList()) {
					str += s;
				}

				pw.getMappInfo().setAuthor(str);
			}

			if (dia.getMimBio().sizeOfContributorArray() > 0) {
				String str = "";

				for (String s : dia.getMimBio().getContributorList()) {
					str += s;
				}

				pw.getMappInfo().setMaintainer(str);
			}

			if (dia.getMimBio().getModified() != null) {
				pw.getMappInfo().setLastModified(
						dia.getMimBio().getModified().toString());
			}
		}

		// Map interactions
		mapInteractionGlyphs();

		// Map entities
		mapSimplePhysicalEntityGlyphs();
		mapEntityFeatureGlyphs();
		mapModifierEntityGlyphs();
		mapConceptualEntityGlyphs();
		mapSourceSinkGlyphs();
		mapRestrictedCopyEntityGlyphs();

		mapGroups();
		mapImplicitComplexEntityGlyphs();

		return pw;
	}

	/**
	 * Map simple physical entity glyphs.
	 */
	private void mapSimplePhysicalEntityGlyphs() {
		for (EntityGlyphType glyph : dia.getEntityGlyphList()) {

			if (glyph.getType().equals("SimplePhysicalEntity")) {

				PathwayElement pwElem = PathwayElement
						.createPathwayElement(ObjectType.DATANODE);

				pwElem.setDataNodeType(DataNodeType
						.create("SimplePhysicalEntity"));

				pwElem.setGraphId(glyph.getVisId());
				pwElem.setTextLabel(glyph.getDisplayName());
				pwElem.setMHeight(glyph.getHeight());
				pwElem.setMWidth(glyph.getWidth());
				pwElem.setMCenterX(glyph.getCenterX());
				pwElem.setMCenterY(glyph.getCenterY());
				pwElem.setGroupRef(glyph.getGroupRef());

				pwElem.setColor(convertHexToColor(glyph.getColor()));

				/*
				 * Set GPML dynamic properties
				 * 
				 * Skip ShapeType because this is known from the entity type
				 */
				for (EntityGlyphType.GenericProperty genProp : glyph
						.getGenericPropertyList()) {
					if (!genProp.getKey().equals("ShapeType")) {
						pwElem.setDynamicProperty(genProp.getKey(),
								genProp.getValue());
					}
				}

				// Set ShapeType
				pwElem.setShapeType(ShapeType.ROUNDED_RECTANGLE);

				// Map RelationshipXRefs
				mapRelationshipXRefs(glyph, pwElem);

				// Map EntityControlledVocabulary
				mapEntityControlledVocabularies(glyph, pwElem);

				pw.add(pwElem);
			}
		}
	}

	/**
	 * Map entity feature glyphs.
	 */
	private void mapEntityFeatureGlyphs() {
		for (EntityGlyphType glyph : dia.getEntityGlyphList()) {

			if (glyph.getType().equals("EntityFeature")) {

				PathwayElement pwElem = PathwayElement
						.createPathwayElement(ObjectType.DATANODE);

				pwElem.setDataNodeType(DataNodeType.create("EntityFeature"));

				pwElem.setGraphId(glyph.getVisId());
				pwElem.setTextLabel(glyph.getDisplayName());
				pwElem.setMHeight(glyph.getHeight());
				pwElem.setMWidth(glyph.getWidth());
				pwElem.setMCenterX(glyph.getCenterX());
				pwElem.setMCenterY(glyph.getCenterY());
				pwElem.setGroupRef(glyph.getGroupRef());

				pwElem.setColor(convertHexToColor(glyph.getColor()));

				/*
				 * Set GPML dynamic properties
				 * 
				 * Skip ShapeType because this is known from the entity type
				 */
				for (EntityGlyphType.GenericProperty genProp : glyph
						.getGenericPropertyList()) {
					if (!genProp.getKey().equals("ShapeType")) {
						pwElem.setDynamicProperty(genProp.getKey(),
								genProp.getValue());
					}
				}

				// Set ShapeType
				pwElem.setShapeType(ShapeType.ROUNDED_RECTANGLE);

				// Map RelationshipXRefs
				mapRelationshipXRefs(glyph, pwElem);

				// Map EntityControlledVocabulary
				mapEntityControlledVocabularies(glyph, pwElem);

				pw.add(pwElem);
			}
		}
	}

	/**
	 * Map modifier entity glyphs.
	 */
	private void mapModifierEntityGlyphs() {
		for (EntityGlyphType glyph : dia.getEntityGlyphList()) {

			if (glyph.getType().equals("Modifier")) {

				PathwayElement pwElem = PathwayElement
						.createPathwayElement(ObjectType.DATANODE);

				pwElem.setDataNodeType(DataNodeType.create("Modifier"));

				pwElem.setGraphId(glyph.getVisId());
				pwElem.setTextLabel(glyph.getDisplayName());
				pwElem.setMHeight(glyph.getHeight());
				pwElem.setMWidth(glyph.getWidth());
				pwElem.setMCenterX(glyph.getCenterX());
				pwElem.setMCenterY(glyph.getCenterY());
				pwElem.setGroupRef(glyph.getGroupRef());

				pwElem.setColor(convertHexToColor(glyph.getColor()));

				/*
				 * Set GPML dynamic properties
				 * 
				 * Skip ShapeType because this is known from the entity type
				 */
				for (EntityGlyphType.GenericProperty genProp : glyph
						.getGenericPropertyList()) {
					if (!genProp.getKey().equals("ShapeType")) {
						pwElem.setDynamicProperty(genProp.getKey(),
								genProp.getValue());
					}
				}

				// Set ShapeType
				pwElem.setShapeType(ShapeType.NONE);

				// Map RelationshipXRefs
				mapRelationshipXRefs(glyph, pwElem);

				// Map EntityControlledVocabulary
				mapEntityControlledVocabularies(glyph, pwElem);

				pw.add(pwElem);
			}
		}
	}

	/**
	 * Map conceptual entity glyphs.
	 */
	private void mapConceptualEntityGlyphs() {
		for (EntityGlyphType glyph : dia.getEntityGlyphList()) {

			if (glyph.getType().equals("ConceptualEntity")) {

				PathwayElement pwElem = PathwayElement
						.createPathwayElement(ObjectType.DATANODE);

				pwElem.setDataNodeType(DataNodeType.create("ConceptualEntity"));

				pwElem.setGraphId(glyph.getVisId());
				pwElem.setTextLabel(glyph.getDisplayName());
				pwElem.setMHeight(glyph.getHeight());
				pwElem.setMWidth(glyph.getWidth());
				pwElem.setMCenterX(glyph.getCenterX());
				pwElem.setMCenterY(glyph.getCenterY());
				pwElem.setGroupRef(glyph.getGroupRef());

				pwElem.setColor(convertHexToColor(glyph.getColor()));

				/*
				 * Set GPML dynamic properties
				 * 
				 * Skip ShapeType because this is known from the entity type
				 */
				for (EntityGlyphType.GenericProperty genProp : glyph
						.getGenericPropertyList()) {
					if (!genProp.getKey().equals("ShapeType")) {
						pwElem.setDynamicProperty(genProp.getKey(),
								genProp.getValue());
					}
				}

				pwElem.setShapeType(ShapeRegistry.fromName("ConceptualEntity"));

				// Map RelationshipXRefs
				mapRelationshipXRefs(glyph, pwElem);

				// Map EntityControlledVocabulary
				mapEntityControlledVocabularies(glyph, pwElem);

				pw.add(pwElem);
			}
		}
	}

	/**
	 * Map source sink glyphs.
	 */
	private void mapSourceSinkGlyphs() {
		for (EntityGlyphType glyph : dia.getEntityGlyphList()) {

			if (glyph.getType().equals("SourceSink")) {

				PathwayElement pwElem = PathwayElement
						.createPathwayElement(ObjectType.DATANODE);

				pwElem.setDataNodeType(DataNodeType.create("SourceSink"));

				pwElem.setGraphId(glyph.getVisId());
				pwElem.setMHeight(glyph.getHeight());
				pwElem.setMWidth(glyph.getWidth());
				pwElem.setMCenterX(glyph.getCenterX());
				pwElem.setMCenterY(glyph.getCenterY());
				pwElem.setGroupRef(glyph.getGroupRef());

				pwElem.setColor(convertHexToColor(glyph.getColor()));

				/*
				 * Set GPML dynamic properties
				 * 
				 * Skip ShapeType because this is known from the entity type
				 */
				for (EntityGlyphType.GenericProperty genProp : glyph
						.getGenericPropertyList()) {
					if (!genProp.getKey().equals("ShapeType")) {
						pwElem.setDynamicProperty(genProp.getKey(),
								genProp.getValue());
					}
				}

				// Set ShapeType
				// pwElem.setShapeType(ShapeType.fromName("SourceSink"));
				pwElem.setShapeType(ShapeRegistry.fromName("SourceSink"));

				/*
				 * DEBUG Make sure that the SourceSink ShapeType has been
				 * registered
				 */
				// String[] shapeTypes = ShapeType.getNames();
				//
				// for (int i = 0; i < shapeTypes.length; i++) {
				// Logger.log.info("ShapeType: " + shapeTypes[i]);
				// }

				// Map RelationshipXRefs
				mapRelationshipXRefs(glyph, pwElem);

				// Map EntityControlledVocabulary
				mapEntityControlledVocabularies(glyph, pwElem);

				pw.add(pwElem);
			}
		}
	}

	/**
	 * Map restricted copy entity glyphs.
	 */
	private void mapRestrictedCopyEntityGlyphs() {
		for (EntityGlyphType glyph : dia.getEntityGlyphList()) {

			if (glyph.getType().equals("RestrictedCopy")) {

				PathwayElement pwElem = PathwayElement
						.createPathwayElement(ObjectType.DATANODE);

				pwElem.setDataNodeType(DataNodeType.create("RestrictedCopy"));

				pwElem.setGraphId(glyph.getVisId());
				pwElem.setMHeight(glyph.getHeight());
				pwElem.setMWidth(glyph.getWidth());
				pwElem.setMCenterX(glyph.getCenterX());
				pwElem.setMCenterY(glyph.getCenterY());
				pwElem.setGroupRef(glyph.getGroupRef());

				pwElem.setColor(convertHexToColor(glyph.getColor()));

				/*
				 * Set GPML dynamic properties
				 * 
				 * Skip ShapeType because this is known from the entity type
				 */
				for (EntityGlyphType.GenericProperty genProp : glyph
						.getGenericPropertyList()) {
					if (!genProp.getKey().equals("ShapeType")) {
						pwElem.setDynamicProperty(genProp.getKey(),
								genProp.getValue());
					}
				}

				// Set ShapeType
				pwElem.setShapeType(ShapeRegistry.fromName("RestrictedCopy"));

				// Needs to be set for the glyph to be filled
				pwElem.setFillColor(convertHexToColor(glyph.getColor()));

				// Map RelationshipXRefs
				mapRelationshipXRefs(glyph, pwElem);

				// Map EntityControlledVocabulary
				mapEntityControlledVocabularies(glyph, pwElem);

				pw.add(pwElem);
			}
		}
	}

	/**
	 * Map interaction glyphs.
	 */
	private void mapInteractionGlyphs() {
		for (InteractionGlyphType glyph : dia.getInteractionGlyphList()) {
			// Diagram.InteractionGlyph.Point@visRef
			// Diagram.InteractionGlyph.Point@visId
			// Diagram.InteractionGlyph.Point@arrowHead
			// Diagram.InteractionGlyph.Anchor@visId

			PathwayElement pwElem = PathwayElement
					.createPathwayElement(ObjectType.LINE);

			pwElem.setColor(convertHexToColor(glyph.getColor()));

			pwElem.setGraphId(glyph.getVisId());
			pwElem.setGroupRef(glyph.getGroupRef());

			// TODO: Find out what comment source looks like in GPML
			for (String com : glyph.getCommentList()) {
				pwElem.addComment(com, "");
			}

			// Set default ConnectorType to Elbow
			if (!glyph.getGenericPropertyList().contains("ConnectorType")) {
				pwElem.setConnectorType(ConnectorType.ELBOW);
				// Logger.log.info("Line doesn't contain ConnectorType");
			}

			// Logger.log.info("Null connector type:" +
			// pwElem.getConnectorType().toString());

			Logger.log.debug("InterGlyph xmlText: " + glyph.xmlText());

			for (InteractionGlyphType.GenericProperty genProp : glyph
					.getGenericPropertyList()) {
				if (genProp.getKey().equals("ConnectorType")) {
					pwElem.setConnectorType(ConnectorType.fromName(genProp
							.getValue()));
				} else {
					pwElem.setDynamicProperty(genProp.getKey(),
							genProp.getValue());
				}
			}

			Logger.log.info("Current line:" + glyph.getVisId() + "\n");
			Logger.log.info("Point list:" + glyph.getPointList().size() + "\n");

			List<MPoint> mPoints = new ArrayList<MPoint>();

			String startGraphRef = null;
			String endGraphRef = null;

			// Map points
			for (int i = 0; i < glyph.getPointList().size(); i++) {
				InteractionGlyphType.Point pt = glyph.getPointArray(i);

				// Map arrowheads
				if (i == 0) {
					String arrowHead = pt.getArrowHead().toString();
					String gpmlArrowHead = ImporterHelper
							.convertArrowHead(arrowHead);

					LineType lt = LineType.fromName(gpmlArrowHead);
					pwElem.setStartLineType(lt);

					Logger.log.info("startLineType:"
							+ pt.getArrowHead().toString() + "\n");
					Logger.log.info("graphRef:" + pt.getVisRef() + "\n");

					startGraphRef = pt.getVisRef();
				}

				if (i < glyph.getPointList().size()) {
					String arrowHead = pt.getArrowHead().toString();
					String gpmlArrowHead = ImporterHelper
							.convertArrowHead(arrowHead);

					LineType lt = LineType.fromName(gpmlArrowHead);
					pwElem.setEndLineType(lt);

					Logger.log.info("endLineType:"
							+ pt.getArrowHead().toString() + "\n");
					Logger.log.info("graphRef:" + pt.getVisRef() + "\n");

					endGraphRef = pt.getVisRef();
				}

				Logger.log.info("point X:" + pt.getX() + "\n");
				Logger.log.info("point Y:" + pt.getY() + "\n");

				MPoint mp = pwElem.new MPoint(pt.getX(), pt.getY());

				mPoints.add(mp);

				Double relX = new Double(Double.NaN);
				Double relY = new Double(Double.NaN);

				Logger.log.info("Importer set RelX: " + pt.getRelX());
				Logger.log.info("Importer set RelY: " + pt.getRelY());

				// Map RelX and RelY
				relX = pt.getRelX();
				relY = pt.getRelY();

				pwElem.setRelX(relX);
				pwElem.setRelY(relY);

				if (!relX.equals(Double.NaN) || !relY.equals(Double.NaN)) {
					mp.setRelativePosition(relX, relY);
				} else {
					mp.setRelativePosition(1.0, 0.5);
				}
			}

			pwElem.setMPoints(mPoints);

			if (isNotBlank(startGraphRef)) {
				pwElem.setStartGraphRef(startGraphRef);
			}

			if (isNotBlank(endGraphRef)) {
				pwElem.setEndGraphRef(endGraphRef);
			}

			Logger.log.info("Points size: " + mPoints.size());

			// Map anchors
			for (String mimAncRef : glyph.getAnchorRefList()) {

				Logger.log.info("mimAncRef: " + mimAncRef);

				AnchorGlyphType mimAnc = getAnchorGlyphType(mimAncRef);

				if (mimAnc != null) {
					MAnchor gpmlAnc = pwElem.addMAnchor(mimAnc.getPosition());
					gpmlAnc.setGraphId(mimAnc.getVisId());

					if (mimAnc.getType().equals("InTrans")) {
						gpmlAnc.setShape(AnchorType.create("Intermolecular",
								true));
					}

					if (mimAnc.getType().equals("Invisible")) {
						gpmlAnc.setShape(AnchorType.NONE);
					}
				} else {
					// TODO: This can be abstracted to other types
					EntityGlyphType mimEc = getExplicitComplexType(mimAncRef);

					MAnchor gpmlAnc = pwElem.addMAnchor(mimEc.getPosition());
					gpmlAnc.setGraphId(mimEc.getVisId());

					gpmlAnc.setShape(AnchorType.CIRCLE);

					Logger.log.debug("Imported Ec: " + mimEc.getVisId());
				}
			}

			// Map PublicationXRefs
			List<String> mimBioRefs = mapPublicationXRefs(glyph, pwElem);
			pwElem.setBiopaxRefs(mimBioRefs);

			pw.add(pwElem);
		}
	}

	private void mapImplicitComplexEntityGlyphs() {
		for (EntityGlyphType glyph : dia.getEntityGlyphList()) {

			if (glyph.getType().equals("ImplicitComplex")) {

				PathwayElement pwElem = PathwayElement
						.createPathwayElement(ObjectType.GROUP);

				pwElem.setGroupId(glyph.getVisId());
				pwElem.setGroupStyle(GroupStyle.fromName("ImplicitComplex"));

				pw.add(pwElem);
			}
		}
	}

	private void mapGroups() {
		for (GroupType grp : dia.getGroupList()) {

			PathwayElement pwElem = PathwayElement
					.createPathwayElement(ObjectType.GROUP);

			// No distinction is made in MIM of one ID type versus the other
			// but in GPML they have different functions.
			pwElem.setGroupId(grp.getVisId());
			pwElem.setGraphId(grp.getVisId());

			if (grp.getType().equals("Generic")) {
				// GroupStyle.GROUP yields "None"
				pwElem.setGroupStyle(GroupStyle.GROUP);
			} else if (grp.getType().equals("EntityWithFeatures")) {
				pwElem.setGroupStyle(GroupStyle.create("EntityWithFeatures",
						true));
			} else {
				Logger.log.error("Unknown group type: " + grp.getType());
			}

			pw.add(pwElem);
		}
	}

	/**
	 * Gets the anchor glyph type.
	 * 
	 * @param anchorRef
	 *            the anchor ref
	 * @return the anchor glyph type
	 */
	private AnchorGlyphType getAnchorGlyphType(String anchorRef) {

		AnchorGlyphType matchedAncGlyph = null;

		Logger.log.debug("Anc anchorRef: " + anchorRef);

		for (AnchorGlyphType ancGlyph : dia.getAnchorList()) {
			if (ancGlyph.getVisId().equals(anchorRef)) {
				matchedAncGlyph = ancGlyph;
			}
		}

		return matchedAncGlyph;
	}

	/**
	 * Gets the explicit complex type.
	 * 
	 * @param anchorRef
	 *            the anchor ref
	 * @return the explicit complex type
	 */
	private EntityGlyphType getExplicitComplexType(String anchorRef) {

		EntityGlyphType matchedEntGlyph = null;

		Logger.log.debug("Ec anchorRef: " + anchorRef);

		for (EntityGlyphType entGlyph : dia.getEntityGlyphList()) {
			if (entGlyph.getVisId().equals(anchorRef)) {
				matchedEntGlyph = entGlyph;
			}
		}

		Logger.log.debug("Matched Ec : " + matchedEntGlyph);

		return matchedEntGlyph;
	}

	/**
	 * Convert arrow head.
	 * 
	 * @param mimArrowHead
	 *            the MIM arrow head
	 * @return the GPML arrowhead type
	 */
	private static String convertArrowHead(String mimArrowHead) {

		BidiMap arrowHash = getGpmlToMimVisArrowHeadMap();

		String gpmlArrowHead;

		// Default to LINE
		if (arrowHash.inverseBidiMap().get(mimArrowHead) != null) {
			gpmlArrowHead = arrowHash.inverseBidiMap().get(mimArrowHead)
					.toString();
		} else {
			gpmlArrowHead = "Line";
			Logger.log.info("Pathway contains an arrow not supported in MIM: "
					+ gpmlArrowHead);
		}

		return gpmlArrowHead;
	}

	/**
	 * Map RelationshipXRefs.
	 * 
	 * @param pwElem
	 *            the pw elem
	 */
	private void mapRelationshipXRefs(EntityGlyphType glyph,
			PathwayElement pwElem) {
		for (String mimBioRef : glyph.getMimBioRefList()) {
			XmlObject o1 = getVisXmlObjectById(visDoc, mimBioRef);

			// Logger.log.debug("RelXRef xmlText: " + xmlObj.xmlText());

			Logger.log.debug("o1.class1 mapRelXRefs: " + o1.getClass());
			Logger.log.debug("o1.text mapRelXRefs: " + o1.xmlText());

			// TODO: This could be made more concise; also for
			// EntityControlledVocabulary and RelationshipXRef
			if (o1 instanceof RelationshipXRefType) {

				RelationshipXRefType o2 = (RelationshipXRefType) o1
						.changeType(gov.nih.nci.lmp.mim.mimVisLevel1.RelationshipXRefType.type);

				Logger.log.debug("o2.class mapRelXRefs: " + o2.getClass());

				if (o2 instanceof RelationshipXRefType) {
					RelationshipXRefType mimRelXRef = o2;

					pwElem.setDataSource(DataSource.getByFullName(mimRelXRef
							.getDb()));
					pwElem.setGeneID(mimRelXRef.getId());

					// DEBUG
					// Logger.log.debug("RelXRef ID: " + mimRelXRef.getId());
					// Logger.log.debug("RelXRef DB: " + mimRelXRef.getDb());

					pwElem.setDynamicProperty("DatabaseRelationship",
							mimRelXRef.getType().toString());
				}
			}

		}
	}

	private List<String> mapPublicationXRefs(InteractionGlyphType glyph,
			PathwayElement pwElem) {

		List<String> mimBioRefs = new ArrayList<String>();

		Logger.log.debug("mimBioRefs mapPubXRefs: "
				+ glyph.sizeOfMimBioRefArray());

		for (String mimBioRef : glyph.getMimBioRefList()) {

			XmlObject o1 = getVisXmlObjectById(visDoc, mimBioRef);

			Logger.log.debug("o1.class1 mapPubXRefs: " + o1.getClass());
			Logger.log.debug("o1.text mapPubXRefs: " + o1.xmlText());

			PublicationXRefType o2 = (PublicationXRefType) o1
					.changeType(gov.nih.nci.lmp.mim.mimVisLevel1.PublicationXRefType.type);

			Logger.log.debug("o2.class mapPubXRefs: " + o2.getClass());

			if (o2 instanceof PublicationXRefType) {

				PublicationXRefType mimPubXRef = o2;

				Logger.log.debug("PubXRef Idx: " + mimPubXRef.getId());

				Logger.log.debug("PubXRef xmlText: " + mimPubXRef.xmlText());

				BiopaxElement refMgr = pw.getBiopax();

				org.pathvisio.core.biopax.PublicationXref gpmlXRef = new PublicationXref();

				Logger.log.debug("PubXRef Id1: " + mimPubXRef.getId());

				// Set publication attributes
				gpmlXRef.setId(mimBioRef);
				gpmlXRef.setPubmedId(mimPubXRef.getId());
				gpmlXRef.setTitle(mimPubXRef.getTitle());
				gpmlXRef.setSource(mimPubXRef.getJournal());
				gpmlXRef.setYear(mimPubXRef.getYear());

				for (String author : mimPubXRef.getAuthorList()) {
					gpmlXRef.addAuthor(author);
					Logger.log.debug("Author: " + author);
				}

				refMgr.addElement(gpmlXRef);

				mimBioRefs.add(mimBioRef);
			}
		}

		return mimBioRefs;
	}

	private void mapEntityControlledVocabularies(EntityGlyphType glyph,
			PathwayElement pwElem) {
		for (String mimBioRef : glyph.getMimBioRefList()) {
			XmlObject xmlObj = getVisXmlObjectById(visDoc, mimBioRef);

			if (xmlObj instanceof EntityControlledVocabularyType) {
				EntityControlledVocabularyType mimEcv = (EntityControlledVocabularyType) xmlObj;
				pwElem.setDynamicProperty("EntityControlledVocabulary", mimEcv
						.getType().toString());
			}
		}
	}
}
