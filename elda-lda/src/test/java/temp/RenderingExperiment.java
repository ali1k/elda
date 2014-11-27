package temp;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import sun.org.mozilla.javascript.WrappedException;

import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.bindings.URLforResource;
import com.epimorphics.lda.core.*;
import com.epimorphics.lda.renderers.velocity.VelocityRenderer;
import com.epimorphics.lda.renderers.velocity.VelocityRendering;
import com.epimorphics.lda.shortnames.CompleteContext.Mode;
import com.epimorphics.lda.shortnames.*;
import com.epimorphics.lda.vocabularies.API;
import com.epimorphics.lda.vocabularies.ELDA_API;
import com.epimorphics.util.CollectionUtils;
import com.epimorphics.util.MediaType;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

public class RenderingExperiment {
	
	static Resource root = ResourceFactory.createResource("eh:/root");

	static Model model = ModelFactory
		.createDefaultModel()
		.add(root, ELDA_API.velocityTemplate, "response.vm")
		;
	
	static Model graphModel = ModelFactory.createDefaultModel();
	
	public static void main(String [] args) throws IOException {

		MediaType mt = MediaType.TEXT_HTML;
		Resource config = model.createResource("eh:/root");
		Mode prefixMode = Mode.PreferPrefixes;
		ShortnameService sns = new StandardShortnameService();
		URLforResource ufr = new URLforResource() {
			
			@Override public URL asResourceURL(String u) {
				try {
					return new URL("file:///tmp/velocity/");
				} catch (MalformedURLException e) {
					throw new WrappedException(e);
				}
			}
		};
		
		Bindings b = new Bindings(new Bindings(), ufr); 
		b.put("_velocityPath", "/tmp/velocity/"); 
		
		List<Resource> noResults = CollectionUtils.list(root.inModel(model));
		Graph resultGraph = graphModel.getGraph();
		
		resultGraph.getPrefixMapping().setNsPrefix("api", API.NS);
		resultGraph.add(Triple.create(root.asNode(), API.items.asNode(), RDF.nil.asNode()));
		
		APIResultSet rs = new APIResultSet(resultGraph, noResults, true, true, "details", View.ALL);
		VelocityRenderer vr = new VelocityRenderer(mt, config, prefixMode, sns);
		
		VelocityRendering vx = new VelocityRendering(b, rs, vr);
		
        VelocityEngine ve = vx.createVelocityEngine();
        VelocityContext vc = vx.createVelocityContext( b );
        
        Writer w = new OutputStreamWriter( System.out, "UTF-8" );
        Template t = ve.getTemplate(vr.templateName());
        
        t.merge(vc,  w);
        w.close();
		
		System.err.println(">> Done.");
	}
	
}
