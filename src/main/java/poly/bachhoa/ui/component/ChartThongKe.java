package poly.bachhoa.ui.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import poly.bachhoa.dao.lmpl.HoaDonDAOImpl;

public class ChartThongKe extends JPanel {

    private HoaDonDAOImpl dao = new HoaDonDAOImpl();
    private static final DecimalFormat MONEY = new DecimalFormat("#,###");
    SimpleDateFormat sdfMonth = new SimpleDateFormat("MM/yyyy");

    public ChartThongKe() {
        setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(610, 260));
    }

    public void updateChart(Date start, Date end, String groupingUnit) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        JFreeChart chart;

        if (start == null || end == null) {
            removeAll();
            add(new JLabel("Vui lòng chọn thời gian thống kê"), BorderLayout.CENTER);
            revalidate();
            repaint();
            return;
        }

        try {
            Map<String, Double> data = dao.getDoanhThuTheoThoiGian(start, end, groupingUnit);

            Calendar cal = Calendar.getInstance();

            switch (groupingUnit.toUpperCase()) {

                case "DAY": {
                    cal.setTime(start);
                    Calendar calEnd = Calendar.getInstance();
                    calEnd.setTime(end);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    while (!cal.after(calEnd)) {
                        String key = sdf.format(cal.getTime());
                        dataset.addValue(data.getOrDefault(key, 0.0), "Doanh thu", key);
                        cal.add(Calendar.DATE, 1);
                    }
                    break;
                }

                case "WEEK": {
                    cal.setTime(start);
                    Calendar calEnd = Calendar.getInstance();
                    calEnd.setTime(end);
                    while (!cal.after(calEnd)) {
                        int week = cal.get(Calendar.WEEK_OF_YEAR);
                        String key = "Tuần " + week;
                        dataset.addValue(data.getOrDefault(key, 0.0), "Doanh thu", key);
                        cal.add(Calendar.DATE, 7); // nhảy tuần
                    }
                    break;
                }

                case "MONTH": {
                    cal.setTime(start);
                    Calendar calEnd = Calendar.getInstance();
                    calEnd.setTime(end);
                    SimpleDateFormat sdfMonth = new SimpleDateFormat("MM/yyyy");
                    while (!cal.after(calEnd)) {
                        String key = sdfMonth.format(cal.getTime());
                        dataset.addValue(data.getOrDefault(key, 0.0), "Doanh thu", key);
                        cal.add(Calendar.MONTH, 1);
                    }
                    break;
                }

                case "QUARTER": {
                    cal.setTime(start);
                    int currentMonth = cal.get(Calendar.MONTH); // 0-11
                    int startQuarter = (currentMonth / 3) * 3; // tháng đầu quý
                    for (int i = startQuarter; i < startQuarter + 3; i++) {
                        cal.set(Calendar.MONTH, i);
                        SimpleDateFormat sdf = new SimpleDateFormat("MM/yyyy");
                        String key = sdf.format(cal.getTime());
                        dataset.addValue(data.getOrDefault(key, 0.0), "Doanh thu", key);
                    }
                    break;
                }

                case "YEAR": {
                    cal.setTime(start);           // lấy năm từ start
                    int year = cal.get(Calendar.YEAR);
                    for (int m = 0; m < 12; m++) {
                        cal.set(Calendar.MONTH, m);
                        cal.set(Calendar.YEAR, year); // set lại năm
                        String key = sdfMonth.format(cal.getTime()); // MM/yyyy
                        dataset.addValue(data.getOrDefault(key, 0.0), "Doanh thu", key);
                    }
                    break;
                }
            }

            // Xác định loại chart
            boolean isLine = groupingUnit.equalsIgnoreCase("WEEK")
                    || groupingUnit.equalsIgnoreCase("QUARTER")
                    || groupingUnit.equalsIgnoreCase("YEAR");

            String xAxisLabel = groupingUnit.equalsIgnoreCase("DAY") ? "Ngày"
                    : groupingUnit.equalsIgnoreCase("WEEK") ? "Tuần"
                    : groupingUnit.equalsIgnoreCase("QUARTER") ? "Quý" : "Tháng/Năm";

            if (isLine) {
                chart = ChartFactory.createLineChart(
                        "Biểu đồ Xu hướng Doanh thu",
                        xAxisLabel,
                        "Doanh thu (VND)",
                        dataset,
                        PlotOrientation.VERTICAL,
                        true, true, false
                );
                CategoryPlot plot = chart.getCategoryPlot();
                LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
                renderer.setDefaultShapesVisible(true);
                renderer.setDefaultItemLabelsVisible(true);
                renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", MONEY));
            } else {
                chart = ChartFactory.createBarChart(
                        "Biểu đồ Doanh thu",
                        xAxisLabel,
                        "Doanh thu (VND)",
                        dataset,
                        PlotOrientation.VERTICAL,
                        true, true, false
                );
                CategoryPlot plot = chart.getCategoryPlot();
                BarRenderer renderer = (BarRenderer) plot.getRenderer();
                renderer.setMaximumBarWidth(0.05);
                renderer.setDefaultItemLabelsVisible(true);
                renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", MONEY));
                plot.getDomainAxis().setCategoryMargin(0.2);
            }

            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setMouseWheelEnabled(true);
            removeAll();
            add(chartPanel, BorderLayout.CENTER);
            revalidate();
            repaint();

        } catch (Exception e) {
            e.printStackTrace();
            removeAll();
            add(new JLabel("Lỗi tải dữ liệu. Xem console!"), BorderLayout.CENTER);
            revalidate();
            repaint();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
