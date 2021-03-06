package com.floreantpos.ui.report;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JRViewer;

import org.jdesktop.swingx.JXDatePicker;

import com.floreantpos.main.Application;
import com.floreantpos.model.util.DateUtil;
import com.floreantpos.report.SalesDetailedReport;
import com.floreantpos.report.SalesReportModelFactory;
import com.floreantpos.report.services.ReportService;
import com.floreantpos.ui.dialog.POSMessageDialog;

public class SalesDetailReportView extends JPanel {
	private SimpleDateFormat fullDateFormatter = new SimpleDateFormat("yyyy MMM dd, hh:mm a");
	private SimpleDateFormat shortDateFormatter = new SimpleDateFormat("yyyy MMM dd");
	
	private JXDatePicker fromDatePicker = new JXDatePicker();
	private JXDatePicker toDatePicker = new JXDatePicker();
	private JButton btnGo = new JButton("GO");
	private JPanel reportContainer;
	
	public SalesDetailReportView() {
		super(new BorderLayout());
		
		JPanel topPanel = new JPanel(new MigLayout());
		
		topPanel.add(new JLabel("From:"), "grow");
		topPanel.add(fromDatePicker,"wrap");
		topPanel.add(new JLabel("To:"), "grow");
		topPanel.add(toDatePicker,"wrap");
		topPanel.add(btnGo, "skip 1, al right");
		add(topPanel, BorderLayout.NORTH);
		
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.setBorder(new EmptyBorder(0, 10,10,10));
		centerPanel.add(new JSeparator(), BorderLayout.NORTH);
		
		reportContainer = new JPanel(new BorderLayout());
		centerPanel.add(reportContainer);
		
		add(centerPanel);
		
		btnGo.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					viewReport();
				} catch (Exception e1) {
					POSMessageDialog.showError(SalesDetailReportView.this, POSMessageDialog.ERROR_MESSAGE, e1);
				}
			}
			
		});
	}
	
	private void viewReport() throws Exception {
		Date fromDate = fromDatePicker.getDate();
		Date toDate = toDatePicker.getDate();
		
		if(fromDate.after(toDate)) {
			POSMessageDialog.showError(Application.getInstance().getBackOfficeWindow(), "From date cannot be greater than to date.");
			return;
		}
		
		fromDate = DateUtil.startOfDay(fromDate);
		toDate = DateUtil.endOfDay(toDate);
		
		ReportService reportService = new ReportService();
		SalesDetailedReport report = reportService.getSalesDetailedReport(fromDate, toDate);
		
		JasperReport drawerPullReport = (JasperReport) JRLoader.loadObject(SalesReportModelFactory.class.getResource("/com/floreantpos/ui/report/sales_summary_balance_detailed__1.jasper"));
		JasperReport creditCardReport = (JasperReport) JRLoader.loadObject(SalesReportModelFactory.class.getResource("/com/floreantpos/ui/report/sales_summary_balance_detailed_2.jasper"));
		
		HashMap map = new HashMap();
		ReportUtil.populateRestaurantProperties(map);
		map.put("fromDate", shortDateFormatter.format(fromDate));
		map.put("toDate", shortDateFormatter.format(toDate));
		map.put("reportTime", fullDateFormatter.format(new Date()));
		map.put("giftCertReturnCount", report.getGiftCertReturnCount());
		map.put("giftCertReturnAmount", report.getGiftCertReturnAmount());
		map.put("giftCertChangeCount", report.getGiftCertChangeCount());
		map.put("giftCertChangeAmount", report.getGiftCertChangeAmount());
		map.put("tipsCount", report.getTipsCount());
		map.put("tipsAmount", report.getChargedTips());
		map.put("tipsPaidAmount", report.getTipsPaid());
		map.put("drawerPullReport", drawerPullReport);
		map.put("drawerPullDatasource", new JRTableModelDataSource(report.getDrawerPullDataTableModel()));
		map.put("creditCardReport", creditCardReport);
		map.put("creditCardReportDatasource", new JRTableModelDataSource(report.getCreditCardDataTableModel()));
		
		JasperReport jasperReport = (JasperReport) JRLoader.loadObject(getClass().getResource("/com/floreantpos/ui/report/sales_summary_balace_detail.jasper"));
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, map, new JREmptyDataSource());
		JRViewer viewer = new JRViewer(jasperPrint);
		reportContainer.removeAll();
		reportContainer.add(viewer);
		reportContainer.revalidate();
		
	}
}
