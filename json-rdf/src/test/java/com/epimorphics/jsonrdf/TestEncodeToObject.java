/*
    See lda-top/LICENCE for the licence for this software.
    (c) copyright Epimorphics Limited 2010
    $Id$
*/

/******************************************************************
    File:        TestEncodeToObject.java
    Created by:  Dave Reynolds
    Created on:  3 Feb 2010
 * 
 * (c) Copyright 2010, Epimorphics Limited
 * $Id:  $
 *****************************************************************/

package com.epimorphics.jsonrdf;

import static org.junit.Assert.*;

import java.io.StringReader;

import org.junit.Test;
import org.openjena.atlas.json.JsonObject;

import com.epimorphics.jsonrdf.utils.ModelIOUtils;
import com.hp.hpl.jena.rdf.model.Model;
/**
 * Test cases the encode to JSONObject cases
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: $
 */
public class TestEncodeToObject {

    @Test public void testModelEncode() {
        Model src = ModelIOUtils.modelFromTurtle(":r :p 42; :q :r2. :r2 :p 24 .");
        JsonObject obj = Encoder.get().encode(src);
        String encoding = obj.toString();
        Model dec = Decoder.decodeModel( new StringReader(encoding) );
        assertTrue( dec.isIsomorphicWith(src) );
    }
}

