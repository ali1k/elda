/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

/*
	(c) Copyright 2010 Epimorphics Limited
	[see end of file]
	$Id$
*/

package com.epimorphics.lda.sources;

import java.util.List;

import com.epimorphics.lda.vocabularies.EXTRAS;
import com.epimorphics.vocabs.API;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.Map1;

import org.slf4j.*;

/**
    A combined source is a way of composing data from different endpoints.
    The combination presents itself as a model which can be queried. The
    endpoints are specified by SPARQL queries themselves.
    
    @author chris
*/
public class CombinedSource implements Source
    {

    static Logger log = LoggerFactory.getLogger( CombinedSource.class );
    
    protected final List<Source> sources;

    protected final List<String> matches;
    
    protected final List<String> constructs;
    
    protected String name;
    
    private static final Map1<Statement, Source> toSource = new Map1<Statement, Source>()
        {
        @Override public Source map1( Statement o )
            { return GetDataSource.sourceFromSpec( o.getResource() ); }
        };

    private static final Map1<Statement, String> toString = new Map1<Statement, String>()
        {
        @Override public String map1( Statement o )
            { return o.getString(); }
        };

    /**
        ep is a resource of type Combiner. It has multiple elements, each of
        which themselves represent sub-sources.
    */
    public CombinedSource( Resource ep )
        {
        constructs = ep.listProperties( EXTRAS.construct ).mapWith( toString ).toList();
        matches = ep.listProperties( EXTRAS.match ).mapWith( toString ).toList();
        sources = ep.listProperties( EXTRAS.element ).mapWith( toSource ).toList();
        }

    @Override public QueryExecution execute( Query query )
        {
        log.info( "doing query execution on a CombinedSource" );
        Model combined = combine();
        return QueryExecutionFactory.create( query, combined );
        }
    
    private Model combine()
        {
        Model combined = ModelFactory.createDefaultModel();
        for (Source s: sources) combined.add( dataFrom( s ) );
        return combined;
        }

    private Model dataFrom( Source s )
        {
        log.info( "getting model data from source " + s );
        String queryString = "construct " + zoob( constructs ) + " where " + zoob( matches ) + "";
        Query mine = QueryFactory.create( queryString );
        QueryExecution q = s.execute( mine );
        return q.execConstruct();
        }

    private String zoob( List<String> ls )
        {
        if (ls.isEmpty())
            return "{?s ?p ?o}";
        else
            {
            String gap = "";
            StringBuilder triples = new StringBuilder();
            for (String s: ls) 
                {
                triples.append( gap ).append( s );
                gap = " . ";
                }
            return "{" + triples.toString() + "}";
            }
        }

    @Override public String toString() {
        if (name == null) {
            StringBuilder buff = new StringBuilder();
            buff.append("combined source [");
            for (Source s : sources) buff.append( s.toString() + " ");
            buff.append("]");
            name = buff.toString();
        }
        return name;
    }
    
    /**
     * Add metdata describing this source to a metdata model 
     */
    public void addMetadata(Resource meta) {
        meta.addProperty(API.sparqlEndpoint, "a combined source");
    }

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
