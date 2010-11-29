package com.epimorphics.lda.endpointspec.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.core.APIEndpointSpec;
import com.epimorphics.lda.core.APISpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class TestItemEndpoint
	{
	Model spec = ModelIOUtils.modelFromTurtle
		( 
		":s a api:API; api:endpoint :e; api:sparqlEndpoint <http://example.com/none>."
		+ "\n:e a api:ItemEndpoint; api:uriTemplate '/absent/friends'; api:itemTemplate 'http://fake.domain.org/spoo/{what}'." 
		);
	
	Resource s = spec.getResource( spec.expandPrefix( ":s" ) );
	Resource e = spec.getResource( spec.expandPrefix( ":e" ) );
	
	@Test public void ensureSpecExtractsItemTemplate()
		{
		APISpec a = new APISpec( s, null );
		APIEndpointSpec eps = new APIEndpointSpec( a, null, e );
		assertEquals( "http://fake.domain.org/spoo/{what}", eps.getItemTemplate() );
		}
	}
