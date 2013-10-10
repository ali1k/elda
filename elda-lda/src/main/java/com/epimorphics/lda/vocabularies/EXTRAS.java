/*
    See lda-top/LICENCE (or http://elda.googlecode.com/hg/LICENCE)
    for the licence for this software.
    
    (c) Copyright 2011 Epimorphics Limited
    $Id$
*/

package com.epimorphics.lda.vocabularies;

import com.hp.hpl.jena.rdf.model.*;

/**
    Place for Elda extension vocabulary items.
*/
public class EXTRAS
    {
	public static String EXTRA = "http://www.epimorphics.com/vocabularies/lda#";
	
	public static String getURI() {
		return EXTRA;
	}

	public static final Property supportsNestedSelect = property( "supportsNestedSelect" );

	public static final Property className = property( "className" );
	
	public static final Property enableETags = property( "enableETags" );
	
    public static final Resource Combiner = resource( "Combiner" );
    
    public static final Property element = property( "element" );

    public static final Property construct = property( "construct" );

    public static final Property match = property( "match" );

    public static final Property describeAllLabel = property( "describeAllLabel" );

    public static final Property enhanceViewWith = property( "enhanceViewWith" );

	public static final Property sparqlQuery = property( "sparqlQuery" );
	
	public static final Property loadedFrom = property( "loadedFrom" );

	public static final Property listURL = property( "listURL" );

	public static final Property metaURL = property( "metaURL" );
	
//

	public static final Property shortnameMode = property( "shortnameMode" );

	public static final Resource roundTrip = resource( "roundTrip" );

	public static final Resource preferLocalnames = resource( "preferLocalnames" );

	public static final Resource preferPrefixes = resource( "preferPrefixes" );
	
//
	
	public static final Property wantsContext = property( "wantsContext" );

	public static final Property cachePolicyName = property( "cachePolicyName" );

	public static final Property metadataOptions = property( "metadataOptions" );

	public static final Property describeThreshold = property( "describeThreshold" );
	
	public static final Property allowReserved = property( "allowReserved" );

	public static final Property allowSyntaxProperties = property( "allowSyntaxProperties" );

	public static final Property uriTemplatePrefix = property( "uriTemplatePrefix" );

	public static final Resource VelocityFormatter = resource( "VelocityFormatter" );

	public static final Property authKey = property( "authKey" );
	
	public static final Property ifStarts = property( "ifStarts" );
	
	public static final Property replaceStartBy = property( "replaceStartBy" );
	
	public static final Property rewriteResultURIs = property( "rewriteResultURIs" );
	
// configuration properties for Elda Atom feeds.
	
	public static final Property feedDateProperties = property( "feedDateProperties" );
	
	public static final Property feedLabelProperties = property( "feedLabelProperties" );
	
	public static final Property feedRightsProperties = property( "feedRightsProperties" );

	public static final Resource FeedFormatter = resource( "FeedFormatter" );
    
	public static final Property feedTitle = property( "feedTitle" );
    
	public static final Property feedRights = property( "feedRights" );
    
	public static final Property feedAuthors = property( "feedAuthors" );
    
	public static final Property feedAuthorProperties = property( "feedAuthorProperties" );

	public static final Property feedNamespace = property( "feedNamespace" );

	public static final Property textQueryProperty = property( "textQueryProperty" );

	public static final Property textContentProperty = property( "textContentProperty" );
	
	public static final Property textSearchOperand = property( "textSearchOperand" );

	public static final Property textPlaceEarly = property( "textPlaceEarly" );

	private static Property property( String local )
        { return ResourceFactory.createProperty( EXTRA + local ); }

    private static Resource resource( String local )
        { return ResourceFactory.createResource( EXTRA + local ); }

    }
    
/*
    (c) Copyright 2010 Epimorphics Limited
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
