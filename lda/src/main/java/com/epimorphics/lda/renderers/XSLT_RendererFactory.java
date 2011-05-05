/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
*/
package com.epimorphics.lda.renderers;

import com.epimorphics.lda.core.APIEndpoint;
import com.epimorphics.lda.core.APIResultSet;
import com.epimorphics.lda.shortnames.ShortnameService;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.rdf.model.Resource;

/**
    Production of XSLT renderers, which transform the results
    of an XML rendering.
*/
public class XSLT_RendererFactory implements RendererFactory {
	
	private final Resource root;
	private final String mediaType;
	
	XSLT_RendererFactory( Resource root, String mediaType ) {
		this.root = root;		
		this.mediaType = mediaType;
	}
	
	@Override public Renderer buildWith( final APIEndpoint ep, final ShortnameService sns ) {
		final String sheet = root.getProperty( API.stylesheet ).getString();
		final XMLRenderer xr = new XMLRenderer( sns, mediaType, sheet );
		return new Renderer() {

			@Override public String getMediaType() {
				return mediaType;
			}

			@Override public String render( RendererContext rc, APIResultSet results ) {
				String rendered = xr.render( rc, results );			
				return rendered.replaceAll( "\"_ROOT", "\"" + rc.getContextPath() );
			}
		}; 
	}

	@Override public RendererFactory withRoot( Resource r ) {
		return new XSLT_RendererFactory( r, mediaType );
	}

	@Override public RendererFactory withMediaType( String mediaType ) {
		return new XSLT_RendererFactory( root, mediaType );
	}
}