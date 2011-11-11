/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

/******************************************************************
    File:        MetadataRestlet.java
    Created by:  Dave Reynolds
    Created on:  14 Feb 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.lda.restlets;

import static com.epimorphics.lda.restlets.ControlRestlet.lookupRequest;
//import static com.epimorphics.lda.restlets.ControlRestlet.lookupRequest;
import static com.epimorphics.lda.restlets.RouterRestlet.returnAs;
import static com.epimorphics.lda.restlets.RouterRestlet.returnNotFound;

import java.io.StringWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.epimorphics.jsonrdf.ContextPropertyInfo;
import com.epimorphics.jsonrdf.Encoder;
import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.bindings.Bindings;
import com.epimorphics.lda.core.View;
import com.epimorphics.lda.core.View.Type;
import com.epimorphics.lda.rdfq.Value;
import com.epimorphics.lda.restlets.ControlRestlet.SpecRecord;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.lda.specs.APIEndpointSpec;
import com.epimorphics.lda.support.PropertyChain;
import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.util.Couple;
import com.epimorphics.util.Util;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.vocabulary.RDFS;


/**
 * Support for viewing the metadata associated with an endpoint.
 * This shows a view of the query that is run and the specification
 * that leads to the query but, unlike the control restlet, doesn't
 * directly link to a dynamically updateable specification.
 * 
 * @author <a href="mailto:der@epimorphics.com">Dave Reynolds</a>
 * @version $Revision: $
 */
@Path("/meta/{path: .*}") public class MetadataRestlet {
    
    @GET @Produces("text/plain")
    public Response requestHandlerPlain( @PathParam("path") String pathstub, @Context UriInfo ui) {
        SpecRecord rec = lookupRequest(pathstub, ui);
        if (rec == null) {
            return returnNotFound("No specification corresponding to path: /" + pathstub);
        } else {
            Resource meta = createMetadata(ui, pathstub, rec);
            return returnAs(ModelIOUtils.renderModelAs(meta.getModel(), "Turtle"), "text/plain");
        }
    }
    
    @GET @Produces("text/turtle")
    public Response requestHandlerTurtle( @PathParam("path") String pathstub, @Context UriInfo ui) {
        SpecRecord rec = lookupRequest(pathstub, ui);
        if (rec == null) {
            return returnNotFound("No specification corresponding to path: /" + pathstub);
        } else {
            Resource meta = createMetadata(ui, pathstub, rec);
            return returnAs(ModelIOUtils.renderModelAs(meta.getModel(), "Turtle"), "text/turtle");
        }
    }
    
    @GET @Produces("application/json")
    public Response requestHandlerJson( @PathParam("path") String pathstub, @Context UriInfo ui) {
        SpecRecord rec = lookupRequest(pathstub, ui);
        if (rec == null) {
            return returnNotFound("No specification corresponding to path: /" + pathstub);
        } else {
            Resource meta = createMetadata(ui, pathstub, rec);
            StringWriter writer = new StringWriter();
            List<Resource> roots = new ArrayList<Resource>(1);
            roots.add( meta );
            com.epimorphics.jsonrdf.Context context = rec.getAPIEndpoint().getSpec().getAPISpec().getShortnameService().asContext();
            context.setSorted(true);
            // false == don't want round-trip context in JSON encoding
            Encoder.getForOneResult( context, false ).encodeRecursive(meta.getModel(), roots, writer, true);
            String enc = writer.toString();
            return returnAs(enc, "application/json");
        }
    }

    static final Property SIBLING = ResourceFactory.createProperty( EXTRAS.EXTRA + "SIBLING" );
    
    private Resource createMetadata(UriInfo ui, String pathStub, SpecRecord rec) {
        Bindings cc = Bindings.createContext( rec.getBindings(), JerseyUtils.convert( ui.getQueryParameters() ) );
        Model metadata = ModelFactory.createDefaultModel();
        Resource meta = rec.getAPIEndpoint().getMetadata( cc, ui.getRequestUri(), metadata);
    //
        for (APIEndpointSpec s: rec.getAPIEndpoint().getSpec().getAPISpec().getEndpoints()) {
            String ut = s.getURITemplate().replaceFirst( "^/", "" );
            if (!ut.equals(pathStub)) {
                String sib = ui
                    .getRequestUri()
                    .toASCIIString()
                    .replace( pathStub, ut )
                    .replace( "%7B", "{" )
                    .replace( "%7D", "}" )
                    ;
                meta.addProperty( SIBLING, metadata.createResource( sib ) );
            }
        }
    // Extract the endpoint specification
        Model spec = rec.getSpecModel();
        Resource endpointSpec = rec.getAPIEndpoint().getSpec().getResource().inModel(spec);
        metadata.setNsPrefixes( spec );
        metadata.add( ResourceUtils.reachableClosure( endpointSpec ) );
        meta.getModel().withDefaultMappings( PrefixMapping.Extended );
        meta.addProperty( API.endpoint, endpointSpec );
        return meta;
    }
    
    @GET @Produces("text/html") public Response requestHandlerHTML( @PathParam("path") String pathstub, @Context UriInfo ui) {
        SpecRecord rec = lookupRequest(pathstub, ui);
        if (rec == null) {
            return returnNotFound("No specification corresponding to path: /" + pathstub);
        } else {
            Resource meta = createMetadata( ui, pathstub, rec );
            StringBuilder textBody = new StringBuilder();
            h2( textBody, "metadata for the API offering " + pathstub );
        //
            renderComments( textBody, rec.getApiRoot() );
        //
            Statement ep = meta.getProperty( API.sparqlEndpoint );
            h3( textBody, "SPARQL endpoint for queries" );
            textBody
                .append( "<div style='margin-left: 2ex; background-color: #dddddd'>" )
                .append( safe( ep.getResource().getURI() ) )
                .append( "</div>" )
                ;
        //
            // renderVariables( textBody, "h2", "API variables", rec.getAPIEndpoint().getSpec().getAPISpec().getBindings() );
        //
            textBody.append( "<h2>endpoints</h2>\n" );
            StringBuilder specBuilder = new StringBuilder();
            List<APIEndpointSpec> endpoints = rec.getAPIEndpoint().getSpec().getAPISpec().getEndpoints();
            Collections.sort( endpoints, sortByEndpointURITemplate );
			for (APIEndpointSpec s: endpoints) renderEndpoint( specBuilder, pathstub, ui, meta.getModel(), s );  
            textBody.append( specBuilder );
        //
            renderDictionary( textBody, meta.getModel(), rec.getAPIEndpoint().getSpec().getAPISpec().getShortnameService() );
        //
            return returnAs( Util.withBody( "Metadata", textBody.toString() ), "text/html" );
        }
    }
    
    private void renderVariables( StringBuilder sb, PrefixMapping pm, String tag, String title, Bindings b ) {
    	List<String> names = new ArrayList<String>( b.keySet() );
    	if (names.size() > 0) {
	    	sb.append( "<" + tag + ">" ).append( title ).append( "</" + tag + ">\n" );
	    	Collections.sort( names );
	    	sb.append( "<div class='indent'>\n" );
	    	sb.append( "<table>\n" );
	    	sb.append( "<thead><tr><th>name</th><th>lexical form</th><th>type or language</th></tr></thead>\n" );
			for (String name: names) {
				Value v = b.get( name );
				String lf = v.spelling() == null ? "<i>none</i>" : v.spelling();
				String type =
					v.type().length() > 0 ? pm.shortForm( v.type() )
					: v.lang().length() > 0 ? v.lang()
					: ""
					;
				sb.append( "<tr>" )
					.append( "<td>" ).append( name ).append( "</td>" )
					.append( "<td>" ).append( safe(lf) ).append( "</td>" )
					.append( "<td>" ).append( type ).append( "</td>" )
					.append( "</tr>\n" )
					;
			}
			sb.append( "</table>\n" );
			sb.append( "</div>\n" );
    	}
	}

	private void renderDictionary( StringBuilder sb, PrefixMapping pm, ShortnameService sns ) {
    	String name = "api:shortNameDictionary";
    	List<String> shortNames = preferredShortnamesInOrder( sns );
		sb.append( "<h2>shortname dictionary <a href='javascript:toggle(\"" + name + "\")'>show/hide</a>" ).append( " </h2>\n" );
		sb.append( "<div id='" + name + "' class='hide'>" );
		sb.append( "<table>\n" );
		sb.append( "<thead><tr><th>short name</th><th>range (if property)</th><th>qname</th></tr></thead>\n" );
		for (String n: shortNames ) {
			String uri = sns.asContext().getURIfromName( n );
			ContextPropertyInfo cpi = sns.asContext().getPropertyByName( n );
			String range = (cpi == null ? "-" : rangeType( pm, cpi.getType() ) );
			sb.append( "<tr>" )
				.append( "<td>" ).append( n ).append( "</td>" )
				.append( "<td>" ).append( range ).append( "</td>" )
				.append( "<td>" ).append( linkFor( pm, uri ) ).append( "</td>\n" )
				.append( "</tr>\n" )
				;
		}
		sb.append( "</table>" );
		sb.append( "</div>\n" );
	}

	private Object linkFor(PrefixMapping pm, String uri) {
		return "<a href='" + uri + "'>" + pm.shortForm( uri ) + "</a>";
	}

	private List<String> preferredShortnamesInOrder(ShortnameService sns) {
		Set<String> allNames = new HashSet<String>( sns.asContext().allNames() );
		Set<String> toRemove = new HashSet<String>();
		for (String oneName: allNames) {
			String uri = sns.asContext().getURIfromName( oneName );
			String preferred = sns.asContext().getNameForURI( uri );
			if (!oneName.equals(preferred)) toRemove.add( oneName );
		}
		allNames.removeAll( toRemove );
		List<String> names = new ArrayList<String>( allNames );
		Collections.sort( names, String.CASE_INSENSITIVE_ORDER );
		return names;
	}
    
    protected String rangeType( PrefixMapping pm, String uri ) {
    	if (uri == null) return "unspecified";
    	return pm.shortForm( uri );    	
    }

    // TODO: metadataoptions, factories
    // link to parent
    void renderEndpoint( StringBuilder sb, String pathStub, UriInfo ui, PrefixMapping pm, APIEndpointSpec s ) {
    	Resource ep = s.getResource();
    	Bindings b = s.getBindings();
    	String ut = ep.getProperty( API.uriTemplate ).getString(); 
    	SpecRecord rec = lookupRequest( safe(ut.substring(1)), ui );
        Resource meta = createMetadata( ui, ut, rec );
        Statement q = meta.getProperty(EXTRAS.sparqlQuery );
        ShortnameService sns = s.sns();
    //
    	renderHeader(sb, s, pathStub, ut);
    	renderComments(sb, s.getResource());
    	renderExampleRequestPath(sb, ep);
    	renderSettings(sb, s);    	
    	renderItemTemplate(sb, s);
    	renderSelectors( sb, ep, q );
    	renderVariables(sb, pm, "h3", "variable bindings for this endpoint", b );
    	renderViews(sb, ep, pm, s, sns);
    	sb.append( "</div>\n" );
    }

	private void renderHeader(StringBuilder sb, APIEndpointSpec s, String pathStub, String ut) {
		String name = ut;
    	String kind = s.isListEndpoint() ? "list" : "item";
    	sb.append( "<div style='font-size: 150%; margin-top: 1ex'>" )
    		.append( " <a href='javascript:toggle(\"" + name + "\")'>" ).append( name ).append( "</a>" )
    		.append( " [" ).append( kind ).append( " endpoint] " )
    		.append( " </div>\n" )
    		;
    	String visibility = matches( pathStub, ut.substring(1) ) ? "show" : "hide";
    	sb.append( "<div id='" + name + "' class='" + visibility + "'>" );
	}

	private boolean matches( String actual, String pattern ) {
		return actual.matches( pattern.replaceAll( "\\{[^}/]*\\}", "[^/]*" ) );
	}

	private void renderComments(StringBuilder sb, Resource spec) {
		List<Statement> commentStatements = spec.listProperties( RDFS.comment ).toList();
    	if (commentStatements.size() > 0) {
    		sb.append( "<h3>comments</h3>\n" );
    		for (Statement cs: commentStatements) {
    			sb.append( "<p>\n" );
    			sb.append( safe( cs.getString() ) );
    			sb.append( "</p>\n" );
    		}
    	}
	}

	private void renderExampleRequestPath(StringBuilder sb, Resource ep) {
		Property API_exampleRequestPath = ep.getModel().createProperty( API.NS, "exampleRequestPath" );
    	List<Statement> examples = ep.listProperties( API_exampleRequestPath ).toList();
    	if (examples.size() > 0) {
    		sb.append( "<h3>example request path(s)" );
    		for (Statement exs: examples) {
    			sb.append( "<div class='indent'>" ).append( safe( exs.getString() ) ).append( "</div>\n" );
    		}
    	}
	}

	private void renderSettings(StringBuilder sb, APIEndpointSpec s) {
		String dl = s.getDefaultLanguage();
    	sb.append( "<h3>settings</h3>\n" );
    	sb.append( "<div class='indent'>" );
    	sb.append( "default page size: " ).append( s.getDefaultPageSize() );
    	sb.append( ", max page size: " ).append( s.getMaxPageSize() );
    	if (dl != null ) sb.append( ", default languages: " ).append( dl );
    	sb.append( ".\n</div>\n" );
	}

	private void renderItemTemplate(StringBuilder sb, APIEndpointSpec s) {
		String it = s.getItemTemplate();
    	if (it != null) {
    		sb.append( "<h3>item template</h3>\n" );
    		sb.append( "<div class='indent'>" ).append( safe( it ) ).append( "</div>\n" );
    	}
	}

	private void renderViews(StringBuilder sb, Resource ep,  PrefixMapping pm, APIEndpointSpec spec, ShortnameService sns) {
		Set<String> viewNames = spec.getExplicitViewNames();
		View dv = spec.getDefaultView();
		boolean seenDefault = false;
		sb.append( "<h3>views</h3>\n" );
		for (String vn: viewNames) {
			View v = spec.getView( vn );
			boolean isDefault = v.equals( dv );
			renderView(sb, sns, pm, isDefault, v);
			if (isDefault) seenDefault = true;
		}
		if (dv != null && !seenDefault) renderView( sb, sns, pm, true, dv);
	}

	private void renderView(StringBuilder sb, ShortnameService sns, PrefixMapping pm, boolean isDefault, View v) {
		String vn = v.name();
		String type = typeAsString( v.getType() );
		sb.append( "<div style='margin-top: 1ex' class='indent'>\n" );
		sb.append( "<div>" );
		if (isDefault) sb.append( "<b>default</b> " );
		sb.append( "<i style='color: red'>" ).append( vn ).append( "</i>" ).append( type );
		sb.append( "</div>" );
		List<PropertyChain> chains = new ArrayList<PropertyChain>( v.chains() );
		List<String> stringedChains = new ArrayList<String>(chains.size());
		for (PropertyChain pc: chains) stringedChains.add( chainToString( sns, pm, pc ) );
		Collections.sort( stringedChains );
		sb.append( "<div class='indent'>\n" );
		for (String pc: stringedChains) {
			sb.append( "<div>").append( pc ).append( "</div>\n" );
		}
		sb.append( "</div>\n" );
		sb.append( "</div>\n" );
	}

	private String typeAsString(Type type) {
		switch (type) {
			case T_ALL: return " (DESCRIBE + labels)";
			case T_DESCRIBE: return " (DESCRIBE)";
			case T_CHAINS: return " (just these properties)";
		}
		return "IMPOSSIBLE";
	}

	private String chainToString( ShortnameService sns, PrefixMapping pm, PropertyChain pc ) {
		StringBuilder sb = new StringBuilder();
		List<Property> properties = pc.getProperties();
		Property last = properties.get( properties.size() - 1 );
		String dot = "";
		for (Property p: properties) {
			sb.append( dot ).append( shortForm( sns, p ) );
			dot = ".";
		}
		ContextPropertyInfo cpi = sns.asContext().findProperty( last );
		if (cpi != null) {
			String type = cpi.getType();
			if (type != null && type.startsWith( "http:" )) {
				String shortName = rangeType( pm, type );
				sb.append( " <i style='color: blue'>" ).append( shortName ).append( "</i>" );
			}
		}
		return sb.toString();
	}

	private void renderSelectors( StringBuilder sb, Resource ep, Statement query ) {
		List<Statement> selectors = ep.listProperties( API.selector ).toList();
		if (selectors.size() > 0) {
			sb.append( "<h3>selectors</h3>\n" );
			for (Statement selector: selectors) 
				renderSelectors(sb, selector.getResource() );
				
		}
//		sb.append( "<h4>generated query</h4> " );
//        sb.append( "\n<pre style='margin-left: 2ex; background-color: #dddddd'>\n" );
//		doSPARQL( sb, query.getString() );
//		sb.append( "</pre>\n" );
	}

	private void renderSelectors( StringBuilder sb, Resource sel ) {
		Set<Couple<String, Resource>> filters = allFiltersOf( new HashSet<Couple<String, Resource>>(), sel );
		for (Couple<String, Resource> filter: filters) {
			String from = (filter.b.equals( sel ) ? "" : " (from " + shortForm( filter.b ) + ")");
			sb.append( "<div class='indent'>" )
				.append( "<b>filter</b> " )
				.append( filter.a )
				.append( from )
				.append( "</div>\n" )
				;
		}
		List<Statement> wheres = sel.listProperties( API.where ).toList();
		for (Statement where: wheres) {
			sb.append( "<div class='indent'>" )
				.append( "<b>where</b>\n" )
				.append( "<pre>" )
				.append( where.getString() )
				.append( "</pre>\n" )
				.append( "</div>\n" )
				;			
		}
	}
    
    protected Set<Couple<String, Resource>> allFiltersOf( Set<Couple<String, Resource>> them, Resource sel) {
    	for (RDFNode p: sel.listProperties( API.parent ).mapWith( Statement.Util.getObject ).toList()) {
    		allFiltersOf( them, (Resource) p );
    	}
    	for (RDFNode f: sel.listProperties( API.filter ).mapWith( Statement.Util.getObject).toList()) {
    		String pvs = ((Literal) f).getLexicalForm();
    		for (String filter: pvs.split( "&" ))
    			them.add( new Couple<String, Resource>( filter, sel ) );
    	}
    	return them;
	}

	private void doSPARQL( StringBuilder sb, String query ) {
		String [] x = query.split( "\nSELECT " );
		if (x.length == 1) { System.err.println( "OOPS: " + query ); x = new String[] { "", query }; }
		String [] prefixes = x[0].split( "\n" );
		Pattern p = Pattern.compile( "([-A-Za-z]+:)" );
		String pp = "", alt = "";
		Matcher m = p.matcher( x[1] );
		while (m.find()) { pp = pp + alt + "PREFIX[ \\t]+" + m.group(0); alt = "|"; }
		Pattern q = Pattern.compile( pp );
		for (String prefix: prefixes)
			if (q.matcher(prefix).find())
				sb.append( safe( prefix ) ).append( "\n" );
		sb.append( safe( "SELECT " + x[1] ) );
	}

    static final Comparator<Statement> byPredicate = new Comparator<Statement>() 
        {
        @Override public int compare( Statement x, Statement y ) 
            { return x.getPredicate().getURI().compareTo( y.getPredicate().getURI() );
            }
        };

    static final Comparator<APIEndpointSpec> sortByEndpointResource = new Comparator<APIEndpointSpec>() {

		@Override public int compare( APIEndpointSpec ep1, APIEndpointSpec ep2 ) {
			return shortForm(ep1.getResource() ).compareTo( shortForm( ep2.getResource() ) );
		}
    };

    static final Comparator<APIEndpointSpec> sortByEndpointURITemplate = new Comparator<APIEndpointSpec>() {

		@Override public int compare( APIEndpointSpec ep1, APIEndpointSpec ep2 ) {
			return ep1.getURITemplate().compareTo( ep2.getURITemplate() );
		}
    };
    
    static final Comparator<Statement> sortByStatementObjectResource = new Comparator<Statement> () {

		@Override public int compare( Statement a, Statement b ) {
			return a.getResource().getURI().compareTo( b.getResource().getURI() );
		}
    };
    
    protected static String shortForm( ShortnameService sns, Resource r ) {
    	String x = sns.asContext().getNameForURI( r.getURI() );
    	return x == null ? shortForm( r ) : x;
    }

	protected static String shortForm( Resource r ) {
		return r.getModel().shortForm( r.getURI() );
	}
	
    private void h1( StringBuilder textBody, String s ) {  
       textBody.append( "\n<h1>" ).append( safe( s ) ).append( "</h1>" );   
    }
        
    private void h2( StringBuilder textBody, String s ) {  
        textBody.append( "\n<h2>" ).append( safe( s ) ).append( "</h2>\n" );        
    }
        
    private void h3( StringBuilder textBody, String s ) {  
        textBody.append( "\n<h3>" ).append( safe( s ) ).append( "</h3>\n" );        
    }
    
    private String safe(String val) {
        return 
        	val.replaceAll( "&", "&amp;" )
        	.replaceAll("<", "&lt;")
        	.replaceAll(">", "&gt;")
        	.replaceAll( "[{]([A-Za-z0-9_]+)[}]", "a_$1" )
        	;
    }
}

