/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

package com.epimorphics.sdx.vocabulary;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class SYSV
    {
    protected static final String ns = "http://epimorphics.com/RDF/vocabularies/System#";
    
    public static final Property currentId = property( "currentId" );

    public static final Resource sysRoot = resource( "sysRoot" );

    public static String getURI()
        { return ns; }
    
    private static Resource resource( String localName )
        { return ResourceFactory.createResource( ns + localName ); }

    private static Property property( String localName )
        { return ResourceFactory.createProperty( ns + localName ); }

    }
