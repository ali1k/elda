/*****************************************************************************
 * Elda project https://github.com/epimorphics/elda
 * LDA spec: http://code.google.com/p/linked-data-api/
 *
 * Copyright (c) 2014 Epimorphics Ltd. All rights reserved.
 * Licensed under the Apache Software License 2.0.
 * Full license: https://raw.githubusercontent.com/epimorphics/elda/master/LICENCE
 *****************************************************************************/

package com.epimorphics.lda.renderers.common;


import static org.junit.Assert.*;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.*;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.epimorphics.lda.renderers.common.DisplayHierarchy.DisplayHierarchyContext;
import com.epimorphics.rdfutil.RDFNodeWrapper;
import com.hp.hpl.jena.rdf.model.*;

/**
 * Unit tests for {@link DisplayHierarchyNode}
 *
 * @author Ian Dickinson, Epimorphics (mailto:ian@epimorphics.com)
 */
public class DisplayHierarchyNodeTest
{
    /***********************************/
    /* Constants                       */
    /***********************************/

    private static final String TEST_ROOT_URI = "http://environment.data.gov.uk/data/bathing-water-quality/in-season/sample/point/03600/date/20140710/time/100000/recordDate/20140710";

    /***********************************/
    /* Static variables                */
    /***********************************/

    static final Model apiMetadataModel = ModelFactory.createDefaultModel();
    static final Model apiObjectModel = ModelFactory.createDefaultModel();
    static final Model apiResultsModel = ModelIOUtils.modelFromTurtle( Fixtures.COMMON_PREFIXES + Fixtures.PAGE_BWQ );

    static final String view_uri = "http://environment.data.gov.uk/doc/bathing-water-quality/in-season/latest.ttl?_lang=en,cy&_view=salmonella&_metadata=all&_page=0";

    /***********************************/
    /* Instance variables              */
    /***********************************/

    @Rule public JUnitRuleMockery context = new JUnitRuleMockery() {{
        // we are forced to use the legacy imposteriser because APIResultSet does not
        // have an interface that it conforms to
        setImposteriser(ClassImposteriser.INSTANCE);

        setThreadingPolicy(new Synchroniser());
    }};

    ResultsModel rm;
    private DisplayHierarchyNode dhn;

    /***********************************/
    /* Constructors                    */
    /***********************************/

    /***********************************/
    /* External signature methods      */
    /***********************************/

    @Before
    public void setUp() throws Exception {
        rm = new ResultsModel( Fixtures.mockResultSet( context, apiResultsModel, apiObjectModel, apiMetadataModel ) );
        Resource root = rm.getModel().getResource( TEST_ROOT_URI );
        DisplayRdfNode dn = new DisplayRdfNode( rm.page(), root );

        dhn = new DisplayHierarchyNode( new PropertyPath(), null, dn );
    }

    @Test
    public void testPathTo() {
        PropertyPath fooBar = new PropertyPath( "foo.bar" );
        DisplayHierarchyNode child = new DisplayHierarchyNode( fooBar, dhn,
                new DisplayRdfNode( rm.page(), ResourceFactory.createPlainLiteral( "foo" ) ) );
        assertSame( fooBar, child.pathTo() );
    }

    @Test
    public void testIsOnExplicitPath() {
        assertFalse( dhn.isOnExplicitPath() );
        dhn.explicitPaths().add( new PropertyPath() );
        assertTrue( dhn.isOnExplicitPath() );
    }

    @Test
    public void testParent() {
        assertNull( dhn.parent() );
        assertTrue( dhn.isRoot() );

        DisplayHierarchyNode child = new DisplayHierarchyNode( new PropertyPath(), dhn,
                                                               new DisplayRdfNode( rm.page(), ResourceFactory.createPlainLiteral( "foo" ) ) );
        assertSame( dhn, child.parent() );
        assertFalse( child.isRoot() );
    }

    @Test
    public void testRdfNode() {
        assertEquals( TEST_ROOT_URI, dhn.rdfNode.getURI() );
    }

    @Test
    public void testIsLoop() {
        Resource r0 = ResourceFactory.createResource( "http://example/foo#r0");
        Resource r1 = ResourceFactory.createResource( "http://example/foo#r1");
        Resource r2 = ResourceFactory.createResource( "http://example/foo#r2");

        DisplayHierarchyNode c0 = new DisplayHierarchyNode( new PropertyPath(), dhn, new DisplayRdfNode( rm.page(), r0 ) );
        DisplayHierarchyNode c1 = new DisplayHierarchyNode( new PropertyPath(), c0, new DisplayRdfNode( rm.page(), r1 ) );
        DisplayHierarchyNode c2 = new DisplayHierarchyNode( new PropertyPath(), c1, new DisplayRdfNode( rm.page(), r2 ) );
        DisplayHierarchyNode c3 = new DisplayHierarchyNode( new PropertyPath(), c1, new DisplayRdfNode( rm.page(), r0 ) );

        assertFalse( dhn.isLoop() );
        assertFalse( c0.isLoop() );
        assertFalse( c1.isLoop() );
        assertFalse( c2.isLoop() );
        assertTrue( c3.isLoop() );

        assertTrue( c0.findAncestor( new DisplayRdfNode( rm.page(), r0 ) ));
        assertTrue( c1.findAncestor( new DisplayRdfNode( rm.page(), r0 ) ));
        assertTrue( c2.findAncestor( new DisplayRdfNode( rm.page(), r0 ) ));
        assertFalse( c1.findAncestor( new DisplayRdfNode( rm.page(), r2 ) ));
    }

    @Test
    public void testIsLeaf() {
        DisplayHierarchyContext dhc = contextFixture( false, "dhc" );

        // not a leaf
        assertFalse( dhn.isLeaf( dhc ));

        // literal is a leaf
        DisplayHierarchyNode c0 = new DisplayHierarchyNode( new PropertyPath(), dhn, new DisplayRdfNode( rm.page(), ResourceFactory.createPlainLiteral( "foo" ) ) );
        assertTrue( c0.isLeaf( dhc ));

        // loop is a leaf
        Resource r0 = ResourceFactory.createResource( "http://example/foo#r0");

        DisplayHierarchyNode c1 = new DisplayHierarchyNode( new PropertyPath(), dhn, new DisplayRdfNode( rm.page(), r0 ) );
        DisplayHierarchyNode c2 = new DisplayHierarchyNode( new PropertyPath(), c1, new DisplayRdfNode( rm.page(), r0 ) );
        assertFalse( c1.isLeaf( dhc ));
        assertTrue( c2.isLeaf( dhc ));

        // seen is a leaf
        DisplayHierarchyContext dhc1 = contextFixture( true, "dhc1" );
        DisplayHierarchyNode c3 = new DisplayHierarchyNode( new PropertyPath(), dhn, new DisplayRdfNode( rm.page(), r0 ) );
        assertTrue( c3.isLeaf( dhc1 ));
    }

    @Test
    public void testChildren() {
        Resource r0 = ResourceFactory.createResource( "http://example/foo#r0");
        DisplayHierarchyNode c1 = new DisplayHierarchyNode( new PropertyPath(), dhn, new DisplayRdfNode( rm.page(), r0 ) );

        assertEquals( 1, dhn.children().size() );
        assertEquals( 0, c1.children().size() );
    }

    /***********************************/
    /* Internal implementation methods */
    /***********************************/

    private DisplayHierarchy.DisplayHierarchyContext contextFixture( final boolean isSeen, String mockName ) {
        final DisplayHierarchy.DisplayHierarchyContext dhContext = context.mock( DisplayHierarchy.DisplayHierarchyContext.class, mockName );
        final RDFNodeWrapper r = new RDFNodeWrapper( rm.page().getModelW(), ResourceFactory.createResource( "http://example/foo#r0") );

        context.checking(new Expectations() {{
            atLeast(0).of (dhContext).isSeen( r );
            will( returnValue( isSeen ) );
        }});

        return dhContext;
    }

    /***********************************/
    /* Inner class definitions         */
    /***********************************/

}
