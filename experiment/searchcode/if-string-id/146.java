package eu.tanov.epf.pv.service.ocl.extension;

import org.eclipse.emf.validation.model.ConstraintSeverity;

/**
 * Something that will hold data for contribution.
 * Fields are equivalent to these:
 * http://help.eclipse.org/helios/topic/org.eclipse.emf.validation.doc/references/extension-points/
 * org_eclipse_emf_validation_constraintProviders.html
 */
public class OCLConstraintsDefinition {
	private final String pluginId;
	private final String id;
	private final String category;
	private final boolean mandatory;
	private final ConstraintSeverity severity;
	private final String content;
	private final String message;

	public OCLConstraintsDefinition(String pluginId, String id, String category, boolean mandatory, ConstraintSeverity severity,
			String content, String message) {
		this.pluginId = pluginId;
		this.id = id;
		this.category = category;
		this.mandatory = mandatory;
		this.severity = severity;
		this.content = content;
		this.message = message;

		if (this.pluginId == null || this.id == null || this.category == null || this.severity == null
				|| this.severity == ConstraintSeverity.NULL || this.content == null || this.message == null) {
			throw new IllegalArgumentException("Not null expected: " + this.toString());
		}
	}

	public String getPluginId() {
		return pluginId;
	}

	public String getId() {
		return id;
	}

	public String getCategory() {
		return category;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public ConstraintSeverity getSeverity() {
		return severity;
	}

	public String getContent() {
		return content;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return String
				.format("OCLConstraintsDefinition(pluginId: %s, id: %s, category: %s, mandatory: %s, severity: %s, content: %s, message: %s)",
						pluginId, id, category, mandatory, severity, content, message);
	}

}

