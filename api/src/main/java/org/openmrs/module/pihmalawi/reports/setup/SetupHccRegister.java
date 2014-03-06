package org.openmrs.module.pihmalawi.reports.setup;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openmrs.api.context.Context;
import org.openmrs.module.pihmalawi.reports.ApzuReportElementsArt;
import org.openmrs.module.pihmalawi.reports.ReportHelper;
import org.openmrs.module.pihmalawi.reports.dataset.HtmlBreakdownDataSetDefinition;
import org.openmrs.module.pihmalawi.reports.renderer.DemographicsOnlyBreakdownRenderer;
import org.openmrs.module.pihmalawi.reports.renderer.HccRegisterBreakdownRenderer;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.PeriodIndicatorReportDefinition;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.reporting.report.util.PeriodIndicatorReportUtil;
import org.openmrs.serialization.SerializationException;

@Deprecated // Replaced with HccRegister report
public class SetupHccRegister {

	ReportHelper h = new ReportHelper();

	public SetupHccRegister(ReportHelper helper) {
		h = helper;
	}

	public ReportDefinition[] setup() throws Exception {
		delete();

		ReportDefinition rd = createReportDefinition("hccreg");
		h.replaceReportDefinition(rd);
		createHtmlBreakdown(rd, "HCC Register_");
//		createDemographicsHtmlBreakdown(rd);

		rd = createReportDefinitionForAllLocations("hccregcomplete");
		h.replaceReportDefinition(rd);
		createHtmlBreakdown(rd, "HCC Register For All Locations_");

		return new ReportDefinition[] { rd };
	}

	protected ReportDesign createHtmlBreakdown(ReportDefinition rd, String name)
			throws IOException, SerializationException {
		Map<String, Mapped<? extends DataSetDefinition>> m = new LinkedHashMap<String, Mapped<? extends DataSetDefinition>>();

		HtmlBreakdownDataSetDefinition dsd = new HtmlBreakdownDataSetDefinition();
		dsd.setPatientIdentifierType(Context.getPatientService()
				.getPatientIdentifierTypeByName("HCC Number"));
		dsd.setHtmlBreakdownPatientRowClassname(HccRegisterBreakdownRenderer.class
				.getName());

		m.put("breakdown", Mapped.mapStraightThrough(dsd));

		return h.createHtmlBreakdown(rd, name, m);
	}

	protected ReportDesign createDemographicsHtmlBreakdown(ReportDefinition rd)
			throws IOException, SerializationException {
		Map<String, Mapped<? extends DataSetDefinition>> m = new LinkedHashMap<String, Mapped<? extends DataSetDefinition>>();

		HtmlBreakdownDataSetDefinition dsd = new HtmlBreakdownDataSetDefinition();
		dsd.setPatientIdentifierType(Context.getPatientService()
				.getPatientIdentifierTypeByName("HCC Number"));
		dsd.setHtmlBreakdownPatientRowClassname(DemographicsOnlyBreakdownRenderer.class
				.getName());

		m.put("breakdown", Mapped.mapStraightThrough(dsd));

		return h.createHtmlBreakdown(rd, "HCC Register Demographics only_", m);
	}

	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if (rd.getName().startsWith("HCC Register")) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeDefinition(DataSetDefinition.class, "HCC Register_ Data Set");
		h.purgeDefinition(ReportDefinition.class, "HCC Register_");
		h.purgeDefinition(DataSetDefinition.class, "HCC Register For All Locations_ Data Set");
		h.purgeDefinition(ReportDefinition.class, "HCC Register For All Locations_");
		h.purgeAll("hccreg");
		h.purgeAll("hccregcomplete");
	}

	private ReportDefinition createReportDefinition(String prefix) {
		PeriodIndicatorReportDefinition rd = new PeriodIndicatorReportDefinition();
		rd.removeParameter(ReportingConstants.START_DATE_PARAMETER);
		rd.setName("HCC Register_");
		rd.setupDataSetDefinition();

		CohortDefinition partEver = ApzuReportElementsArt.partEverEnrolledAtLocationOnDate(prefix);
		CohortDefinition exposedEver = ApzuReportElementsArt.exposedEverEnrolledAtLocationOnDate(prefix);
		CohortDefinition cd = ApzuReportElementsArt
				.hccEverEnrolledAtLocationOnDate(prefix, partEver, exposedEver);

		CohortIndicator i = h.newCountIndicator(prefix + "Register_", cd
				.getName(), h.parameterMap("location", "${location}",
				"startedOnOrBefore", "${endDate}"));
		PeriodIndicatorReportUtil
				.addColumn(rd, "breakdown", "Breakdown", i, null);

		return rd;
	}
	
	private ReportDefinition createReportDefinitionForAllLocations(String prefix) {
		PeriodIndicatorReportDefinition rd = new PeriodIndicatorReportDefinition();
		rd.removeParameter(ReportingConstants.START_DATE_PARAMETER);
		rd.removeParameter(ReportingConstants.LOCATION_PARAMETER);
		rd.setName("HCC Register For All Locations (SLOW)_");
		rd.setupDataSetDefinition();

		CohortDefinition partEver = ApzuReportElementsArt.partEverEnrolledOnDate(prefix);
		CohortDefinition exposedEver = ApzuReportElementsArt.exposedEverEnrolledOnDate(prefix);
		CohortDefinition cd = ApzuReportElementsArt
				.hccEverEnrolledOnDate(prefix, partEver, exposedEver);

		CohortIndicator i = h.newCountIndicator(prefix + ": Register For All Locations", cd
				.getName(), h.parameterMap(
				"startedOnOrBefore", "${endDate}"));
		PeriodIndicatorReportUtil
				.addColumn(rd, "breakdown", "Breakdown", i, null);

		return rd;
	}

}
