package com.mycompany.gephitoolkit9;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;
import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.api.Partition;
import org.gephi.appearance.api.PartitionFunction;
import org.gephi.appearance.plugin.PartitionElementColorTransformer;
import org.gephi.appearance.plugin.RankingElementColorTransformer;
import org.gephi.appearance.plugin.RankingLabelSizeTransformer;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.appearance.plugin.palette.Palette;
import org.gephi.appearance.plugin.palette.PaletteManager;
import org.gephi.filters.api.FilterController;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.AutoLayout;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.presets.BlackBackground;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

public class SNtoTransform {

    private UndirectedGraph graph;
    private AppearanceModel appearanceModel;
    private AppearanceController appearanceController;
    private GraphModel graphModel;

    private void changeColorByDegree() {
        Function degreeRanking = appearanceModel.getNodeFunction(graph, AppearanceModel.GraphFunction.NODE_DEGREE, RankingElementColorTransformer.class);
        RankingElementColorTransformer degreeTransformer = (RankingElementColorTransformer) degreeRanking.getTransformer();
        degreeTransformer.setColors(new Color[]{new Color(0xFEF0D9), new Color(0xB30000)});
        degreeTransformer.setColorPositions(new float[]{0f, 1f});
        appearanceController.transform(degreeRanking);
    }

    private void changeColorByColumn(String columnName) {
        int columnId = -1;

        for (Column col : graphModel.getNodeTable()) {
            if (columnName.equals(col.getTitle()) ){
                columnId = col.getIndex();
                break;
            }
        }

        if (columnId != -1) {
            Column countryColumn = graphModel.getNodeTable().getColumn(columnId);
            Function func = appearanceModel.getNodeFunction(graph, countryColumn, PartitionElementColorTransformer.class);
            Partition partition = ((PartitionFunction) func).getPartition();
            Palette palette = PaletteManager.getInstance().generatePalette(partition.size());
            partition.setColors(palette.getColors());
            appearanceController.transform(func);
        }

    }

    private void changeSizeByCentrality() {
        GraphDistance distance = new GraphDistance();
        distance.setDirected(true);
        distance.execute(graphModel);

        Column centralityColumn = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
        Function centralityRanking = appearanceModel.getNodeFunction(graph, centralityColumn, RankingNodeSizeTransformer.class);
        RankingNodeSizeTransformer centralityTransformer = (RankingNodeSizeTransformer) centralityRanking.getTransformer();
        centralityTransformer.setMinSize(5);
        centralityTransformer.setMaxSize(30);
        appearanceController.transform(centralityRanking);
    }

    private void changeSizeByDegree() {
        Function degreeRanking = appearanceModel.getNodeFunction(graph, AppearanceModel.GraphFunction.NODE_DEGREE, RankingNodeSizeTransformer.class);
        RankingNodeSizeTransformer degreeTransformer = (RankingNodeSizeTransformer) degreeRanking.getTransformer();
        degreeTransformer.setMinSize(2);
        degreeTransformer.setMaxSize(40);
        appearanceController.transform(degreeRanking);
    }

    private void changeLabelSizeByDegree() {
        Function labelRanking = appearanceModel.getNodeFunction(graph, AppearanceModel.GraphFunction.NODE_DEGREE, RankingLabelSizeTransformer.class);
        RankingLabelSizeTransformer labelTransformer = (RankingLabelSizeTransformer) labelRanking.getTransformer();
        labelTransformer.setMinSize(0.4f);
        labelTransformer.setMaxSize(0.5f);
        appearanceController.transform(labelRanking);
    }

    private void changeLayoutForceAtlasYifan() {
        AutoLayout autoLayout = new AutoLayout(1, TimeUnit.MINUTES);
        autoLayout.setGraphModel(graphModel);

        YifanHuLayout firstLayout = new YifanHuLayout(null, new StepDisplacement(1f));
        autoLayout.addLayout(firstLayout, 0.5f);

        ForceAtlasLayout secondLayout = new ForceAtlasLayout(null);
        AutoLayout.DynamicProperty adjustBySizeProperty = AutoLayout.createDynamicProperty("forceAtlas.adjustSizes.name", Boolean.TRUE, 0.01f);//True after 10% of layout time
        AutoLayout.DynamicProperty repulsionProperty = AutoLayout.createDynamicProperty("forceAtlas.repulsionStrength.name", 150., 0f);
        AutoLayout.DynamicProperty atractionProperty = AutoLayout.createDynamicProperty("forceAtlas.AttractionStrength.name", 0.2, 0f);
        AutoLayout.DynamicProperty atractionDistributionProperty = AutoLayout.createDynamicProperty("forceAtlas.OutboundAttractionDistribution.name", Boolean.TRUE, 0f);
        AutoLayout.DynamicProperty gravityProperty = AutoLayout.createDynamicProperty("forceAtlas.Gravity.name", 50., 0f);
        AutoLayout.DynamicProperty InertiaProperty = AutoLayout.createDynamicProperty("forceAtlas.Inertia.name", 0.1, 0f);
        autoLayout.addLayout(secondLayout, 0.5f, new AutoLayout.DynamicProperty[]{adjustBySizeProperty, repulsionProperty, atractionDistributionProperty, atractionProperty, gravityProperty, InertiaProperty});

        autoLayout.execute();
    }

    private void changeLayoutForceAtlas() {
        AutoLayout autoLayout = new AutoLayout(10, TimeUnit.SECONDS);
        autoLayout.setGraphModel(graphModel);
        //YifanHuLayout firstLayout = new YifanHuLayout(null, new StepDisplacement(1f));
        ForceAtlasLayout secondLayout = new ForceAtlasLayout(null);
        AutoLayout.DynamicProperty adjustBySizeProperty = AutoLayout.createDynamicProperty("forceAtlas.adjustSizes.name", Boolean.TRUE, 0.01f);//True after 10% of layout time
        AutoLayout.DynamicProperty repulsionProperty = AutoLayout.createDynamicProperty("forceAtlas.repulsionStrength.name", 150., 0f);
        AutoLayout.DynamicProperty atractionProperty = AutoLayout.createDynamicProperty("forceAtlas.AttractionStrength.name", 0.2, 0f);
        AutoLayout.DynamicProperty atractionDistributionProperty = AutoLayout.createDynamicProperty("forceAtlas.OutboundAttractionDistribution.name", Boolean.TRUE, 0f);
        AutoLayout.DynamicProperty gravityProperty = AutoLayout.createDynamicProperty("forceAtlas.Gravity.name", 60., 0f);
        AutoLayout.DynamicProperty InertiaProperty = AutoLayout.createDynamicProperty("forceAtlas.Inertia.name", 0.1, 0f);
        autoLayout.addLayout(secondLayout, 1f, new AutoLayout.DynamicProperty[]{adjustBySizeProperty, repulsionProperty, atractionDistributionProperty, atractionProperty, gravityProperty, InertiaProperty});
        autoLayout.execute();
    }

    private void exportToType(Workspace workspace, String type) {
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);

        switch (type) {

            case "PDF":
                try {
                    ec.exportFile(new File("outputSNImage.pdf"));
                } catch (IOException ex) {
                }
                break;

            case "GRAPHML":
                Exporter exporterGraphML = ec.getExporter("graphml");     //Get GraphML exporter
                exporterGraphML.setWorkspace(workspace);
                StringWriter stringWriter = new StringWriter();
                try {
                    ec.exportWriter(stringWriter, (CharacterExporter) exporterGraphML);
                    ec.exportFile(new File("outputSN.graphml"), exporterGraphML);
                } catch (IOException ex) {
                }
                break;

            case "GEXF":
                GraphExporter exporter = (GraphExporter) ec.getExporter("gexf");     //Get GEXF exporter
                exporter.setExportVisible(true);  //Only exports the visible (filtered) graph
                exporter.setWorkspace(workspace);
                try {
                    ec.exportFile(new File("../JavaScript/AuthorsSN.gexf"), exporter);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return;
                }
        }

    }

    private void configurePreview(PreviewModel model) {
        model.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
        //model.getProperties().putValue(PreviewProperty.NODE_LABEL_MAX_CHAR, 2.0f);
        model.getProperties().putValue(PreviewProperty.EDGE_COLOR, new EdgeColor(Color.GRAY));
        model.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, 1.2f);
        model.getProperties().putValue(PreviewProperty.NODE_BORDER_WIDTH, 0.3f);
        //model.getProperties().putValue(PreviewProperty.NODE_LABEL_FONT, model.getProperties().getFontValue(PreviewProperty.NODE_LABEL_FONT).deriveFont(1));        
        //model.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.TRUE);

        //model.getProperties().applyPreset(new BlackBackground());
    }

    public void script() {

        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Get models and controllers for this new workspace - will be useful later
        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
        appearanceModel = appearanceController.getModel();

        //Import file       
        Container container;
        try {
            //File file = new File(getClass().getResource("/org/gephi/toolkit/demos/polblogs.gml").toURI());
            File file = new File("simpleSN.graphml");
            container = importController.importFile(file);
            container.getLoader().setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);   //Force DIRECTED
        } catch (Exception ex) {
            return;
        }

        //Append imported data to GraphAPI
        importController.process(container, new DefaultProcessor(), workspace);

        //See if graph is well imported
        graph = graphModel.getUndirectedGraph();
        System.out.println("Nodes: " + graph.getNodeCount());
        System.out.println("Edges: " + graph.getEdgeCount());

        UndirectedGraph graphVisible = graphModel.getUndirectedGraphVisible();
        System.out.println("Nodes: " + graphVisible.getNodeCount());
        System.out.println("Edges: " + graphVisible.getEdgeCount());

        //======================================================================
        changeLayoutForceAtlas();
        //changeLayoutForceAtlasYifan();
        //======================================================================
        changeColorByColumn("País");

        //======================================================================
        changeSizeByDegree();

        //======================================================================
        configurePreview(model);

        //======================================================================
        changeLabelSizeByDegree();
        //======================================================================

        //exportToType(workspace, "GRAPHML");
        exportToType(workspace, "GEXF");
    }
}
