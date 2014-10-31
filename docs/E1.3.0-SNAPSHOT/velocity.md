---
title: Velocity renderer
layout: default-toc
---

Velocity template rendering
===========================

This release of Elda includes the ability to use
[Velocity](http://velocity.apache.org/) templates for rendering. This
feature is *provisional*; it may change significantly in future
releases.

To use the Velocity renderer to generate HTML, attach this formatter to
your API spec:

    <yourSpec> api:defaultFormatter
        [a elda:VelocityFormatter
        ; api:name "html"
        ; elda:className "com.epimorphics.lda.renderers.VelocityRendererFactory"
        ; api:mimeType "text/html"
        ]

By default, this will render the template `page-shell.vm` found in
`{_velocityRoot}` if that is defined, or in `vm` if not, where `vm` is
relative to this webapp's `webapp` directory. Note that `_velocityRoot`
may be a URL (so themplates can be fetched from anywhere on the web) or
an absolute path (so templates can be fetched from anywhere on the local
filing system).

To change the rendered template, set the `elda:velocityTemplate`
property of the formatter to the name of the desired template.

You may choose to specify a Velocity formatter as a property of an
endpoint rather than as the API-wide default.

You can change the associated suffix of the formatter by changing the
value of its `api:name` property, and change the content-type of the
generated page by changing the value of the api:mimeType property.

Elda looks for `{velocity.properties` in the velocity root, and, if it
exists and is non-empty, uses it as its Velocity configuration. If
there's no such file, or it contains no property definitions, Elda uses
a default configuration that fetches templates from `{_velocityRoot}` if
it is defined and from `vm` if not.

The example Velocity renderer
-----------------------------

![](elda-velocity-screenshot-1.png)

The Elda title bar links to the RDF specification, the Elda
documentation, the Linked Data API specification, and the Epimorphics
web page.

Underneath that is the list of available formats that this endpoint can
display information in; clicking on list items rerenders this page in
the format. Since Elda's XSLT-driven HTML renderer and the Velocity
renderer both generate HTML but only one can be *called* HTML, the
original renderer gets the "html" name and the new one is named "vhtml".

Below the format list the page is displayed in two columns; the left one
for search results (information about selected items) and the right one
for additional viewing information (map, filters, views and viewing
properties).

At the top of the search results Elda displays links to the items on
this page, and buttons to move through the item list. Each item entry
names the item (displaying its preferred label) and shows selected
properties of that item. Values that are themselves resources with
properties have their properties displayed *unless* they have already
appeared in an outer display box. Clicking on an item's name will
restrict the display to that single item.

Clicking on the operator symbols (\<\<=\>\>) toward the right of the
search results will restrict the search to items that have the specified
value for the specified property. These filters will then appear in the
**filters** section of the **viewing** column, where they can be snipped
away using the **cancel** icon.

The **properties** section of **viewing** shows properties that have
been explicitly selected for display. Those properties can be discarded
using the **cancel** icon, extended with ".\*" using the **star** icon,
or have a trailing ".\*" cut away using the **reduce** icon. The
**star** icon on the **properties** title introduces the wildcard
property chain "\*" into the proeprties if it's not already present.

The **views** section displays the available views (*ie*, named sets of
properties) for this endpoint. Clicking on the **expand** icon will
toggle displaying the properties of that view; clicking on the **plus**
icon of that property will add it to the **properties** display.
Clicking on the **plus** icon of the view will set it (and hence all its
properties) as the current view.

If there are any items in the top-level of the search results that have
both a `lat` and `long` property, then a map is displayed in the
**viewing** column with markers at those locations.

Names in the Velocity context
-----------------------------

When rendering a page using Velocity, Elda binds several names in the
context:

name

value

thisPage

A WrappedNode for a resource with the URI of this page.

isItemEndpoint

A boolean, true iff this page is for an item endpoint.

isListEndpoint

A boolean, true iff this page is for a list endpoint.

primaryTopic

A WrappedNode for the primary topic of this page, only defined if this
page is for an item endpoint.

names

a map from resources to their short names, passed when needed to methods
on `WrappedNode`s.

formats

a list of Format objects in order of their names. Each Format has a
getName and getLink method; a format's Link is the URI needed to fetch
the version of the current page in this format.

items

the list of selected items. Each item is a `WrappedNode`.

meta

A map from string pathnames to `WrappedNode` values. Each pathname is
the dot-separated concatenation of the short names of the properties in
the path. The proeprty chains are those of the metadata in the result
model, disregarding the `termBinding`s and the `variableValues` since
these have their own context variables.

vars

A map from LDA variable names to `WrappedNode`s representing their
values.

filters

A map of the name=parameter query parameter values (discarding all the
ones that start with "\_").

ids

An IdMap object which contains a map from Resources to their identifiers
for use in HTML `id=` attributes.

utils

Utility methods not appropriate for placing in WrappedNode or
WrappedString.

utils.println(Object)

One-argument println to System.err, as a debugging aid. Output starts
with "\>\>".

utils.println(Object, Object)

Two-argument space-separated println to System.err, as a debugging aid.
Output starts with "\>\>".

utils.sort(Object c)

Sorts the sortable collection c in-place.

utils.join(Collection c, String infix)

Return an infix-separated concatenation of the toString() values of the
elements of c.

utils.currentMillis()

Returns the current time in milliseconds since the Velocity renderer was
initialised for this rendering, meant as a debugging/profiling aid.

utils

Wrapped RDFNodes
----------------

WrappedNodes are wrappers round Jena RDFNodes. When they are created,
they are given a ShortNames object to allow them to render their short
names and an IdMap object to hold their allocated Id. They have the
following methods:

  -------------------------------------------------------------------------
  signature
  description
  ------------------------------------ ------------------------------------
  boolean equals(Object other)         String getId()
  A WrappedNode is .equals to another  Answer the id of this WrappedNode,
  object if that object is a           allocating a fresh one if necessary.
  WrappedNode and their underlying     
  RDFNodes are equal.                  
  -------------------------------------------------------------------------

Where a String result might contain HTML-significant characters,
WrappedNodes return a WrappedString object.

  -------------------------------------------------------------------------
  signature
  description
  ------------------------------------ ------------------------------------
  WrappedString cut()                  String toString()
  Returns a new wrapped string who's   Return the content of this
  content is the content of this       WrappedString, performing HTML
  wrapped string but with spaces       escaping.
  inserted in place of runs of '\_'    
  and between a lower-case letter      
  followed by an upper-case one, with  
  that letter converted to lower-case. 
  -------------------------------------------------------------------------

* * * * *

© Copyright 2011–2013 Epimorphics Limited. For licencing conditions see
<http://http://epimorphics.github.io/elda/LICENCE.html>.
