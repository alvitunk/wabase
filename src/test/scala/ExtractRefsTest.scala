package test

import org.scalatest.funsuite.AnyFunSuite
import io.swagger.v3.oas.models._
import io.swagger.v3.oas.models.media._
import io.swagger.v3.oas.models.responses._
import io.swagger.v3.oas.models.examples._
import io.swagger.v3.oas.models.parameters._
import io.swagger.v3.oas.models.headers._
import io.swagger.v3.oas.models.links._
import io.swagger.v3.oas.models.callbacks._
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.servers.ServerVariables
import io.swagger.v3.oas.models.servers.ServerVariable
import scala.jdk.CollectionConverters._
import org.wabase.swagger.WabaseSwaggerGenerator
import org.mojoz.querease.Querease
import org.mojoz.metadata.ViewDef
import org.wabase.AppMetadata.RouteDef

import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.info.Info


import scala.jdk.CollectionConverters._

class ExtractRefsTest extends AnyFunSuite {
  private def generator = new WabaseSwaggerGenerator(
    qes = Seq.empty[Querease],
    hostString = "localhost",
    isRelevantView = (_: ViewDef) => true,
    isRelevantRoute = (_: RouteDef) => true
  )

  test("PathItem with only $ref") {
    val pathItem = new PathItem()
    pathItem.set$ref("#/components/pathItems/Path1")
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/pathItems/Path1"))
  }

  test("PathItem with GET operation containing Parameter with $ref") {
    val param = new Parameter()
    param.set$ref("#/components/parameters/Param1")
    val op = new Operation()
    op.setParameters(List(param).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/parameters/Param1"))
  }

  test("PathItem with GET operation containing RequestBody with $ref") {
    val rb = new RequestBody()
    rb.set$ref("#/components/requestBodies/RB1")
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/requestBodies/RB1"))
  }

  test("PathItem with GET operation containing ApiResponses with one $ref") {
    val response = new ApiResponse()
    response.set$ref("#/components/responses/Resp1")
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/responses/Resp1"))
  }

  test("PathItem with GET operation containing Callback with PathItem $ref") {
    val cbPath = new PathItem()
    cbPath.set$ref("#/components/pathItems/CallbackPath")
    val callback = new Callback()
    callback.addPathItem("cb", cbPath)
    val op = new Operation()
    op.setCallbacks(Map("cb" -> callback).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/pathItems/CallbackPath"))
  }

  test("PathItem with GET operation containing Server list") {
    val server = new Server()
    val op = new Operation()
    op.setServers(List(server).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs.isEmpty)
  }

  test("PathItem with Parameter containing Schema with $ref") {
    val schema = new Schema[Object]()
    schema.set$ref("#/components/schemas/Schema1")
    val param = new Parameter()
    param.setSchema(schema)
    val pathItem = new PathItem()
    pathItem.setParameters(List(param).asJava)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/Schema1"))
  }

  test("PathItem with Parameter containing Content with MediaType with Schema $ref") {
    val schema = new Schema[Object]()
    schema.set$ref("#/components/schemas/Schema2")
    val mt = new MediaType()
    mt.setSchema(schema)
    val content = new Content()
    content.addMediaType("application/json", mt)
    val param = new Parameter()
    param.setContent(content)
    val pathItem = new PathItem()
    pathItem.setParameters(List(param).asJava)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/Schema2"))
  }

  test("PathItem with GET operation containing RequestBody with Content with MediaType with Schema $ref") {
    val schema = new Schema[Object]()
    schema.set$ref("#/components/schemas/Schema3")
    val mt = new MediaType()
    mt.setSchema(schema)
    val content = new Content()
    content.addMediaType("application/json", mt)
    val rb = new RequestBody()
    rb.setContent(content)
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/Schema3"))
  }

  test("PathItem with GET operation containing ApiResponse with Header and Link with $ref") {
    val header = new Header()
    header.set$ref("#/components/headers/Header1")
    val link = new Link()
    link.set$ref("#/components/links/Link1")
    val response = new ApiResponse()
    response.setHeaders(Map("h" -> header).asJava)
    response.setLinks(Map("l" -> link).asJava)
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/headers/Header1", "#/components/links/Link1"))
  }

  test("PathItem with Parameter containing schema with properties containing $ref") {
    val propSchema = new Schema[Object]()
    propSchema.set$ref("#/components/schemas/PropSchema")
    val schema = new Schema[Object]()
    schema.setProperties(Map("prop" -> propSchema).asJava.asInstanceOf[java.util.Map[String, Schema[_]]])
    val param = new Parameter()
    param.setSchema(schema)
    val pathItem = new PathItem()
    pathItem.setParameters(List(param).asJava)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/PropSchema"))
  }

  test("PathItem with Parameter containing schema with additionalProperties containing $ref") {
    val additionalSchema = new Schema[Object]()
    additionalSchema.set$ref("#/components/schemas/AdditionalSchema")
    val schema = new Schema[Object]()
    schema.setAdditionalProperties(additionalSchema)
    val param = new Parameter()
    param.setSchema(schema)
    val pathItem = new PathItem()
    pathItem.setParameters(List(param).asJava)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/AdditionalSchema"))
  }

  test("PathItem with Parameter containing schema with items containing $ref") {
    val itemSchema = new Schema[Object]()
    itemSchema.set$ref("#/components/schemas/ItemSchema")
    val schema = new Schema[Object]()
    schema.setItems(itemSchema)
    val param = new Parameter()
    param.setSchema(schema)
    val pathItem = new PathItem()
    pathItem.setParameters(List(param).asJava)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/ItemSchema"))
  }

  test("PathItem with Parameter containing schema with allOf containing $ref") {
    val allOfSchema = new Schema[Object]()
    allOfSchema.set$ref("#/components/schemas/AllOfSchema")
    val schema = new Schema[Object]()
    schema.setAllOf(List(allOfSchema).asJava.asInstanceOf[java.util.List[Schema[_]]])
    val param = new Parameter()
    param.setSchema(schema)
    val pathItem = new PathItem()
    pathItem.setParameters(List(param).asJava)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/AllOfSchema"))
  }

  test("PathItem with Parameter containing schema with anyOf containing $ref") {
    val anyOfSchema = new Schema[Object]()
    anyOfSchema.set$ref("#/components/schemas/AnyOfSchema")
    val schema = new Schema[Object]()
    schema.setAnyOf(List(anyOfSchema).asJava.asInstanceOf[java.util.List[Schema[_]]])
    val param = new Parameter()
    param.setSchema(schema)
    val pathItem = new PathItem()
    pathItem.setParameters(List(param).asJava)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/AnyOfSchema"))
  }

  test("PathItem with Parameter containing schema with oneOf containing $ref") {
    val oneOfSchema = new Schema[Object]()
    oneOfSchema.set$ref("#/components/schemas/OneOfSchema")
    val schema = new Schema[Object]()
    schema.setOneOf(List(oneOfSchema).asJava.asInstanceOf[java.util.List[Schema[_]]])
    val param = new Parameter()
    param.setSchema(schema)
    val pathItem = new PathItem()
    pathItem.setParameters(List(param).asJava)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/OneOfSchema"))
  }

  test("PathItem with GET operation containing ApiResponse with content with MediaType with schema $ref") {
    val schema = new Schema[Object]()
    schema.set$ref("#/components/schemas/ResponseSchema")
    val mt = new MediaType()
    mt.setSchema(schema)
    val content = new Content()
    content.addMediaType("application/json", mt)
    val response = new ApiResponse()
    response.setContent(content)
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/ResponseSchema"))
  }

  test("PathItem with GET operation containing ApiResponse with header with schema with $ref") {
    val schema = new Schema[Object]()
    schema.set$ref("#/components/schemas/HeaderSchema")
    val header = new Header()
    header.setSchema(schema)
    val response = new ApiResponse()
    response.setHeaders(Map("h" -> header).asJava)
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/HeaderSchema"))
  }

  test("PathItem with GET operation containing ApiResponse with content with MediaType with example with $ref") {
    val example = new Example()
    example.set$ref("#/components/examples/Example1")
    val mt = new MediaType()
    mt.setExamples(Map("ex" -> example).asJava)
    val content = new Content()
    content.addMediaType("application/json", mt)
    val response = new ApiResponse()
    response.setContent(content)
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/examples/Example1"))
  }

  test("PathItem with GET operation containing RequestBody with schema with allOf and anyOf with $ref") {
    val s1 = new Schema[Object]()
    s1.set$ref("#/components/schemas/AllOf")
    val s2 = new Schema[Object]()
    s2.set$ref("#/components/schemas/AnyOf")
    val schema = new Schema[Object]()
    schema.setAllOf(List(s1).asJava.asInstanceOf[java.util.List[Schema[_]]])
    schema.setAnyOf(List(s2).asJava.asInstanceOf[java.util.List[Schema[_]]])
    val mt = new MediaType()
    mt.setSchema(schema)
    val content = new Content()
    content.addMediaType("application/json", mt)
    val rb = new RequestBody()
    rb.setContent(content)
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/AllOf", "#/components/schemas/AnyOf"))
  }

  test("PathItem with GET operation containing RequestBody with schema with oneOf with $ref") {
    val s = new Schema[Object]()
    s.set$ref("#/components/schemas/OneOfSchema")
    val schema = new Schema[Object]()
    schema.setOneOf(List(s).asJava.asInstanceOf[java.util.List[Schema[_]]])
    val mt = new MediaType()
    mt.setSchema(schema)
    val content = new Content()
    content.addMediaType("application/json", mt)
    val rb = new RequestBody()
    rb.setContent(content)
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/OneOfSchema"))
  }

  test("PathItem with GET operation containing RequestBody with schema with not with $ref") {
    val notSchema = new Schema[Object]()
    notSchema.set$ref("#/components/schemas/NotSchema")
    val schema = new Schema[Object]()
    schema.setNot(notSchema)
    val mt = new MediaType()
    mt.setSchema(schema)
    val content = new Content()
    content.addMediaType("application/json", mt)
    val rb = new RequestBody()
    rb.setContent(content)
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/NotSchema"))
  }

  test("PathItem with GET operation containing RequestBody with schema with properties with $ref") {
    val propSchema = new Schema[Object]()
    propSchema.set$ref("#/components/schemas/PropSchema")
    val schema = new Schema[Object]()
    schema.setProperties(Map("p" -> propSchema).asJava.asInstanceOf[java.util.Map[String, Schema[_]]])
    val mt = new MediaType()
    mt.setSchema(schema)
    val content = new Content()
    content.addMediaType("application/json", mt)
    val rb = new RequestBody()
    rb.setContent(content)
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/PropSchema"))
  }

  test("PathItem with GET operation containing RequestBody with schema with additionalProperties with $ref") {
    val additionalSchema = new Schema[Object]()
    additionalSchema.set$ref("#/components/schemas/AdditionalSchema")
    val schema = new Schema[Object]()
    schema.setAdditionalProperties(additionalSchema)
    val mt = new MediaType()
    mt.setSchema(schema)
    val content = new Content()
    content.addMediaType("application/json", mt)
    val rb = new RequestBody()
    rb.setContent(content)
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/AdditionalSchema"))
  }

  test("PathItem with GET operation containing RequestBody with schema with items with $ref") {
    val itemSchema = new Schema[Object]()
    itemSchema.set$ref("#/components/schemas/ItemSchema")
    val schema = new Schema[Object]()
    schema.setItems(itemSchema)
    val mt = new MediaType()
    mt.setSchema(schema)
    val content = new Content()
    content.addMediaType("application/json", mt)
    val rb = new RequestBody()
    rb.setContent(content)
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/ItemSchema"))
  }

  test("PathItem with GET operation containing RequestBody with schema with nested not inside allOf with $ref") {
    val inner = new Schema[Object]()
    inner.set$ref("#/components/schemas/InnerNot")
    val outer = new Schema[Object]()
    outer.setNot(inner)
    val schema = new Schema[Object]()
    schema.setAllOf(List(outer).asJava.asInstanceOf[java.util.List[Schema[_]]])
    val mt = new MediaType()
    mt.setSchema(schema)
    val content = new Content()
    content.addMediaType("application/json", mt)
    val rb = new RequestBody()
    rb.setContent(content)
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/InnerNot"))
  }

  test("PathItem with GET operation containing RequestBody with schema with nested items and properties with $ref") {
    val nested = new Schema[Object]()
    nested.set$ref("#/components/schemas/Nested")
    val schema = new Schema[Object]()
    schema.setItems(nested)
    schema.setProperties(Map("p" -> nested).asJava.asInstanceOf[java.util.Map[String, Schema[_]]])
    val mt = new MediaType()
    mt.setSchema(schema)
    val content = new Content()
    content.addMediaType("application/json", mt)
    val rb = new RequestBody()
    rb.setContent(content)
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/Nested"))
  }

  test("PathItem with GET operation containing RequestBody with schema with all composition types with same $ref") {
    val shared = new Schema[Object]()
    shared.set$ref("#/components/schemas/Shared")
    val schema = new Schema[Object]()
    schema.setAllOf(List(shared).asJava.asInstanceOf[java.util.List[Schema[_]]])
    schema.setAnyOf(List(shared).asJava.asInstanceOf[java.util.List[Schema[_]]])
    schema.setOneOf(List(shared).asJava.asInstanceOf[java.util.List[Schema[_]]])
    val mt = new MediaType()
    mt.setSchema(schema)
    val content = new Content()
    content.addMediaType("application/json", mt)
    val rb = new RequestBody()
    rb.setContent(content)
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/Shared"))
  }

  test("PathItem with GET operation containing RequestBody with schema with empty composition lists") {
    val schema = new Schema[Object]()
    schema.setAllOf(List.empty.asJava.asInstanceOf[java.util.List[Schema[_]]])
    schema.setAnyOf(List.empty.asJava.asInstanceOf[java.util.List[Schema[_]]])
    schema.setOneOf(List.empty.asJava.asInstanceOf[java.util.List[Schema[_]]])
    val mt = new MediaType()
    mt.setSchema(schema)
    val content = new Content()
    content.addMediaType("application/json", mt)
    val rb = new RequestBody()
    rb.setContent(content)
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs.isEmpty)
  }

  test("PathItem with POST operation containing Parameter with $ref") {
    val param = new Parameter()
    param.set$ref("#/components/parameters/PostParam")
    val op = new Operation()
    op.setParameters(List(param).asJava)
    val pathItem = new PathItem()
    pathItem.setPost(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/parameters/PostParam"))
  }

  test("PathItem with PUT operation containing RequestBody with $ref") {
    val rb = new RequestBody()
    rb.set$ref("#/components/requestBodies/PutRB")
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setPut(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/requestBodies/PutRB"))
  }

  test("PathItem with DELETE operation containing ApiResponse with $ref") {
    val response = new ApiResponse()
    response.set$ref("#/components/responses/DeleteResp")
    val responses = new ApiResponses()
    responses.addApiResponse("204", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setDelete(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/responses/DeleteResp"))
  }

  test("PathItem with GET operation containing multiple Parameters with mixed $ref and inline") {
    val p1 = new Parameter()
    p1.set$ref("#/components/parameters/P1")
    val p2 = new Parameter() // no $ref
    val op = new Operation()
    op.setParameters(List(p1, p2).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/parameters/P1"))
  }

  test("PathItem with GET operation containing multiple ApiResponses with mixed $ref and inline") {
    val r1 = new ApiResponse()
    r1.set$ref("#/components/responses/R1")
    val r2 = new ApiResponse() // no $ref
    val responses = new ApiResponses()
    responses.addApiResponse("200", r1)
    responses.addApiResponse("400", r2)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/responses/R1"))
  }

  test("PathItem with GET operation containing ApiResponse with Header with content with MediaType with schema $ref") {
    val schema = new Schema[Object]()
    schema.set$ref("#/components/schemas/HeaderContentSchema")
    val mt = new MediaType()
    mt.setSchema(schema)
    val content = new Content()
    content.addMediaType("application/json", mt)
    val header = new Header()
    header.setContent(content)
    val response = new ApiResponse()
    response.setHeaders(Map("h" -> header).asJava)
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/HeaderContentSchema"))
  }

  test("PathItem with GET operation containing Callback with multiple PathItems with $ref") {
    val cb1 = new PathItem()
    cb1.set$ref("#/components/pathItems/CB1")
    val cb2 = new PathItem()
    cb2.set$ref("#/components/pathItems/CB2")
    val callback = new Callback()
    callback.addPathItem("cb1", cb1)
    callback.addPathItem("cb2", cb2)
    val op = new Operation()
    op.setCallbacks(Map("cb" -> callback).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/pathItems/CB1", "#/components/pathItems/CB2"))
  }

  test("PathItem with GET operation containing null Parameters and Callbacks") {
    val op = new Operation()
    op.setParameters(null)
    op.setCallbacks(null)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs.isEmpty)
  }

  test("PathItem with GET operation containing empty ApiResponses map") {
    val responses = new ApiResponses() // no responses added
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs.isEmpty)
  }


  test("PathItem with GET operation containing RequestBody with content with multiple MediaTypes with schema $ref") {
    val s1 = new Schema[Object]()
    s1.set$ref("#/components/schemas/JsonSchema")
    val s2 = new Schema[Object]()
    s2.set$ref("#/components/schemas/XmlSchema")

    val mt1 = new MediaType()
    mt1.setSchema(s1.asInstanceOf[Schema[_]])
    val mt2 = new MediaType()
    mt2.setSchema(s2.asInstanceOf[Schema[_]])

    val content = new Content()
    content.addMediaType("application/json", mt1)
    content.addMediaType("application/xml", mt2)

    val rb = new RequestBody()
    rb.setContent(content)

    val op = new Operation()
    op.setRequestBody(rb)

    val pathItem = new PathItem()
    pathItem.setGet(op)

    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/JsonSchema", "#/components/schemas/XmlSchema"))
  }

  test("PathItem with GET operation containing ApiResponse with Header with schema using not with $ref") {
    val notSchema = new Schema[Object]()
    notSchema.set$ref("#/components/schemas/NotHeaderSchema")
    val schema = new Schema[Object]()
    schema.setNot(notSchema)
    val header = new Header()
    header.setSchema(schema)
    val response = new ApiResponse()
    response.setHeaders(Map("h" -> header).asJava)
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/NotHeaderSchema"))
  }

  test("PathItem with GET operation containing ApiResponse with Header schema with allOf with $ref") {
    val s1 = new Schema[Object]()
    s1.set$ref("#/components/schemas/H1")
    val schema = new Schema[Object]()
    schema.setAllOf(List(s1).asJava.asInstanceOf[java.util.List[Schema[_]]])
    val header = new Header()
    header.setSchema(schema)
    val response = new ApiResponse()
    response.setHeaders(Map("h" -> header).asJava)
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/H1"))
  }

  test("PathItem with GET operation containing ApiResponse with Header schema with anyOf with $ref") {
    val s1 = new Schema[Object]()
    s1.set$ref("#/components/schemas/H2")
    val schema = new Schema[Object]()
    schema.setAnyOf(List(s1).asJava.asInstanceOf[java.util.List[Schema[_]]])
    val header = new Header()
    header.setSchema(schema)
    val response = new ApiResponse()
    response.setHeaders(Map("h" -> header).asJava)
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/H2"))
  }

  test("PathItem with GET operation containing ApiResponse with Header schema with oneOf with $ref") {
    val s1 = new Schema[Object]()
    s1.set$ref("#/components/schemas/H3")
    val schema = new Schema[Object]()
    schema.setOneOf(List(s1).asJava.asInstanceOf[java.util.List[Schema[_]]])
    val header = new Header()
    header.setSchema(schema)
    val response = new ApiResponse()
    response.setHeaders(Map("h" -> header).asJava)
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/H3"))
  }

  test("PathItem with GET operation containing ApiResponse with Header schema with not with $ref") {
    val s1 = new Schema[Object]()
    s1.set$ref("#/components/schemas/H4")
    val schema = new Schema[Object]()
    schema.setNot(s1)
    val header = new Header()
    header.setSchema(schema)
    val response = new ApiResponse()
    response.setHeaders(Map("h" -> header).asJava)
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/H4"))
  }

  test("PathItem with GET operation containing ApiResponse with Header content with multiple MediaTypes with schema $ref") {
    val s1 = new Schema[Object]()
    s1.set$ref("#/components/schemas/H5")
    val s2 = new Schema[Object]()
    s2.set$ref("#/components/schemas/H6")
    val mt1 = new MediaType()
    mt1.setSchema(s1.asInstanceOf[Schema[_]])
    val mt2 = new MediaType()
    mt2.setSchema(s2.asInstanceOf[Schema[_]])
    val content = new Content()
    content.addMediaType("json", mt1)
    content.addMediaType("xml", mt2)
    val header = new Header()
    header.setContent(content)
    val response = new ApiResponse()
    response.setHeaders(Map("h" -> header).asJava)
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/H5", "#/components/schemas/H6"))
  }

  test("PathItem with GET operation containing ApiResponse with Header content with MediaType with example $ref") {
    val example = new Example()
    example.set$ref("#/components/examples/HeaderExample")
    val mt = new MediaType()
    mt.setExamples(Map("ex" -> example).asJava)
    val content = new Content()
    content.addMediaType("json", mt)
    val header = new Header()
    header.setContent(content)
    val response = new ApiResponse()
    response.setHeaders(Map("h" -> header).asJava)
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/examples/HeaderExample"))
  }

  test("PathItem with GET operation containing RequestBody with content with MediaType with example $ref") {
    val example = new Example()
    example.set$ref("#/components/examples/BodyExample")
    val mt = new MediaType()
    mt.setExamples(Map("ex" -> example).asJava)
    val content = new Content()
    content.addMediaType("json", mt)
    val rb = new RequestBody()
    rb.setContent(content)
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/examples/BodyExample"))
  }

  test("PathItem with GET operation containing Link with $ref") {
    val link = new Link()
    link.set$ref("#/components/links/LinkRef")
    val response = new ApiResponse()
    response.setLinks(Map("l" -> link).asJava)
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/links/LinkRef"))
  }

  test("PathItem with GET operation containing Link with operationId and parameters") {
    val link = new Link()
    link.setOperationId("getUser")
    link.setParameters(Map("userId" -> "id").asJava)
    val response = new ApiResponse()
    response.setLinks(Map("l" -> link).asJava)
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs.isEmpty)
  }

  test("PathItem with GET operation containing MediaType with encoding with schema $ref") {
    val schema = new Schema[Object]()
    schema.set$ref("#/components/schemas/EncodingSchema")
    val mt = new MediaType()
    mt.setSchema(schema)
    val encoding = new Encoding()
    mt.setEncoding(Map("enc" -> encoding).asJava)
    val content = new Content()
    content.addMediaType("json", mt)
    val rb = new RequestBody()
    rb.setContent(content)
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/EncodingSchema"))
  }

  test("PathItem with GET operation containing MediaType with empty encoding and examples") {
    val mt = new MediaType()
    mt.setEncoding(Map.empty[String, Encoding].asJava)
    mt.setExamples(Map.empty[String, Example].asJava)
    val content = new Content()
    content.addMediaType("json", mt)
    val rb = new RequestBody()
    rb.setContent(content)
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs.isEmpty)
  }

  test("PathItem with GET operation containing Parameter with schema with nested not inside allOf with $ref") {
    val inner = new Schema[Object]()
    inner.set$ref("#/components/schemas/DeepNot")
    val outer = new Schema[Object]()
    outer.setNot(inner)
    val schema = new Schema[Object]()
    schema.setAllOf(List(outer).asJava.asInstanceOf[java.util.List[Schema[_]]])
    val param = new Parameter()
    param.setSchema(schema)
    val op = new Operation()
    op.setParameters(List(param).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/DeepNot"))
  }

  test("PathItem with GET operation containing ApiResponse with Link with $ref") {
    val link = new Link()
    link.set$ref("#/components/links/OpRef")
    val response = new ApiResponse()
    response.setLinks(Map("l" -> link).asJava)
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/links/OpRef"))
  }


  test("PathItem with GET operation containing ApiResponse with Link with operationRef and parameters") {
    val link = new Link()
    link.setOperationRef("#/components/links/OpRef")
    link.setParameters(Map("id" -> "$response.body#/id").asJava)
    val response = new ApiResponse()
    response.setLinks(Map("l" -> link).asJava)
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/links/OpRef"))
  }

  test("PathItem with GET operation containing Callback with empty path item map") {
    val callback = new Callback() // empty by default
    val op = new Operation()
    op.setCallbacks(Map("cb" -> callback).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs.isEmpty)
  }


  test("PathItem with GET operation containing ApiResponse with null headers and links") {
    val response = new ApiResponse()
    response.setHeaders(null)
    response.setLinks(null)
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs.isEmpty)
  }

  test("PathItem with GET operation containing RequestBody with null content") {
    val rb = new RequestBody()
    rb.setContent(null)
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs.isEmpty)
  }

  test("PathItem with GET operation containing Parameter with content with MediaType with schema with allOf and $ref") {
    val s1 = new Schema[Object]()
    s1.set$ref("#/components/schemas/ParamAllOf")
    val schema = new Schema[Object]()
    schema.setAllOf(List(s1).asJava.asInstanceOf[java.util.List[Schema[_]]])
    val mt = new MediaType()
    mt.setSchema(schema)
    val content = new Content()
    content.addMediaType("json", mt)
    val param = new Parameter()
    param.setContent(content)
    val op = new Operation()
    op.setParameters(List(param).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/ParamAllOf"))
  }

  test("PathItem with GET operation containing Parameter with schema with additionalProperties set to true") {
    val schema = new Schema[Object]()
    schema.setAdditionalProperties(true)
    val param = new Parameter()
    param.setSchema(schema)
    val op = new Operation()
    op.setParameters(List(param).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs.isEmpty)
  }

  test("PathItem with GET operation containing Parameter with null schema and content") {
    val param = new Parameter()
    param.setSchema(null)
    param.setContent(null)
    val op = new Operation()
    op.setParameters(List(param).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs.isEmpty)
  }

  test("PathItem with GET operation containing MediaType with Encoding with Header with $ref") {
    val header = new Header()
    header.set$ref("#/components/headers/EncHeader")
    val encoding = new Encoding()
    encoding.setHeaders(Map("h" -> header).asJava)
    val mt = new MediaType()
    mt.setEncoding(Map("enc" -> encoding).asJava)
    val content = new Content()
    content.addMediaType("json", mt)
    val rb = new RequestBody()
    rb.setContent(content)
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/headers/EncHeader"))
  }

  test("PathItem with GET operation containing MediaType with Encoding with Header with schema $ref") {
    val schema = new Schema[Object]()
    schema.set$ref("#/components/schemas/EncHeaderSchema")
    val header = new Header()
    header.setSchema(schema)
    val encoding = new Encoding()
    encoding.setHeaders(Map("h" -> header).asJava)
    val mt = new MediaType()
    mt.setEncoding(Map("enc" -> encoding).asJava)
    val content = new Content()
    content.addMediaType("json", mt)
    val rb = new RequestBody()
    rb.setContent(content)
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/EncHeaderSchema"))
  }

  test("PathItem with GET operation containing Schema with nested allOf and anyOf with $ref") {
    val s1 = new Schema[Object]()
    s1.set$ref("#/components/schemas/NestedA")
    val s2 = new Schema[Object]()
    s2.set$ref("#/components/schemas/NestedB")
    val allOf = new Schema[Object]()
    allOf.setAllOf(List(s1).asJava.asInstanceOf[java.util.List[Schema[_]]])
    val anyOf = new Schema[Object]()
    anyOf.setAnyOf(List(s2).asJava.asInstanceOf[java.util.List[Schema[_]]])
    val schema = new Schema[Object]()
    schema.setAllOf(List(allOf, anyOf).asJava.asInstanceOf[java.util.List[Schema[_]]])
    val param = new Parameter()
    param.setSchema(schema)
    val op = new Operation()
    op.setParameters(List(param).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/NestedA", "#/components/schemas/NestedB"))
  }

  test("PathItem with GET operation containing MediaType with example with $ref") {
    val example = new Example()
    example.set$ref("#/components/examples/BodyExample")
    val mt = new MediaType()
    mt.setExamples(Map("example1" -> example).asJava)
    val content = new Content()
    content.addMediaType("application/json", mt)
    val rb = new RequestBody()
    rb.setContent(content)
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/examples/BodyExample"))
  }


  test("PathItem with GET operation containing Parameter with content with exploded encoding and schema $ref") {
    val schema = new Schema[Object]()
    schema.set$ref("#/components/schemas/ExplodedSchema")
    val mt = new MediaType()
    mt.setSchema(schema)
    val encoding = new Encoding()
    encoding.setExplode(true)
    mt.setEncoding(Map("enc" -> encoding).asJava)
    val content = new Content()
    content.addMediaType("json", mt)
    val param = new Parameter()
    param.setContent(content)
    val op = new Operation()
    op.setParameters(List(param).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/ExplodedSchema"))
  }

  test("PathItem with GET operation containing ApiResponse with multiple Headers with $ref") {
    val h1 = new Header()
    h1.set$ref("#/components/headers/H1")
    val h2 = new Header()
    h2.set$ref("#/components/headers/H2")
    val response = new ApiResponse()
    response.setHeaders(Map("h1" -> h1, "h2" -> h2).asJava)
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/headers/H1", "#/components/headers/H2"))
  }

  test("PathItem with GET operation containing ApiResponse with multiple Links with $ref") {
    val l1 = new Link()
    l1.set$ref("#/components/links/L1")
    val l2 = new Link()
    l2.set$ref("#/components/links/L2")
    val response = new ApiResponse()
    response.setLinks(Map("l1" -> l1, "l2" -> l2).asJava)
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/links/L1", "#/components/links/L2"))
  }

  test("PathItem with GET operation containing Callback with PathItem containing Operation with RequestBody with $ref") {
    val rb = new RequestBody()
    rb.set$ref("#/components/requestBodies/CBBody")
    val op = new Operation()
    op.setRequestBody(rb)
    val cbPath = new PathItem()
    cbPath.setPost(op)
    val callback = new Callback()
    callback.addPathItem("cb", cbPath)
    val mainOp = new Operation()
    mainOp.setCallbacks(Map("cb" -> callback).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(mainOp)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/requestBodies/CBBody"))
  }

  test("PathItem with GET operation containing Callback with PathItem with $ref and nested Operation") {
    val cbPath = new PathItem()
    cbPath.set$ref("#/components/pathItems/CBRef")
    val callback = new Callback()
    callback.addPathItem("cb", cbPath)
    val op = new Operation()
    op.setCallbacks(Map("cb" -> callback).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/pathItems/CBRef"))
  }

  test("PathItem with GET operation containing RequestBody with MediaType with Encoding with null headers") {
    val encoding = new Encoding()
    encoding.setHeaders(null)
    val mt = new MediaType()
    mt.setEncoding(Map("enc" -> encoding).asJava)
    val content = new Content()
    content.addMediaType("json", mt)
    val rb = new RequestBody()
    rb.setContent(content)
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs.isEmpty)
  }

  test("PathItem with GET operation containing SecurityRequirement with no refs") {
    val secReq = new SecurityRequirement()
    secReq.addList("apiKey")
    val op = new Operation()
    op.setSecurity(List(secReq).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs.isEmpty)
  }

  test("PathItem with GET operation containing Server with description referencing schema via Parameter") {
    val nestedSchema = new Schema[Object]()
    nestedSchema.set$ref("#/components/schemas/ServerExtensionSchema")

    val paramSchema = new Schema[Object]()
    paramSchema.setProperties(Map("nested" -> nestedSchema).asJava.asInstanceOf[java.util.Map[String, Schema[_]]])


    val param = new Parameter()
    param.setName("serverParam")
    param.setIn("query")
    param.setSchema(paramSchema)

    val server = new Server()
    server.setUrl("https://api.example.com")
    server.setDescription("Uses serverParam for configuration")

    val op = new Operation()
    op.setServers(List(server).asJava)
    op.setParameters(List(param).asJava)

    val pathItem = new PathItem()
    pathItem.setGet(op)

    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/ServerExtensionSchema"))
  }


  test("PathItem with GET operation containing Parameter with schema with nested items with $ref") {
    val items = new Schema[Object]()
    items.set$ref("#/components/schemas/NestedItems")
    val schema = new Schema[Object]()
    schema.setItems(items)
    val param = new Parameter()
    param.setSchema(schema)
    val op = new Operation()
    op.setParameters(List(param).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/NestedItems"))
  }

  test("PathItem with GET operation containing RequestBody with multiple MediaTypes with schema $ref") {
    val s1 = new Schema[Object]()
    s1.set$ref("#/components/schemas/MT1")
    val s2 = new Schema[Object]()
    s2.set$ref("#/components/schemas/MT2")
    val mt1 = new MediaType()
    mt1.setSchema(s1)
    val mt2 = new MediaType()
    mt2.setSchema(s2)
    val content = new Content()
    content.addMediaType("application/json", mt1)
    content.addMediaType("application/xml", mt2)
    val rb = new RequestBody()
    rb.setContent(content)
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/MT1", "#/components/schemas/MT2"))
  }

  test("PathItem with GET operation containing ApiResponse with Header with schema and content with $ref") {
    val schema = new Schema[Object]()
    schema.set$ref("#/components/schemas/HSchema")
    val header = new Header()
    header.setSchema(schema)
    val mt = new MediaType()
    mt.setSchema(schema)
    val content = new Content()
    content.addMediaType("application/json", mt)
    header.setContent(content)
    val response = new ApiResponse()
    response.setHeaders(Map("h" -> header).asJava)
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/HSchema"))
  }

  test("PathItem with GET operation containing ApiResponse with Link using operationId and parameter expression") {
    val link = new Link()
    link.setOperationId("getUserById")
    link.setParameters(Map("userId" -> "$request.path.id").asJava)
    val response = new ApiResponse()
    response.setLinks(Map("userLink" -> link).asJava)
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs.isEmpty) // operationId is not a $ref, so no refs should be collected
  }


  test("PathItem with GET operation containing Parameter with schema with additionalProperties with $ref") {
    val ap = new Schema[Object]()
    ap.set$ref("#/components/schemas/AdditionalProps")
    val schema = new Schema[Object]()
    schema.setAdditionalProperties(ap)
    val param = new Parameter()
    param.setSchema(schema)
    val op = new Operation()
    op.setParameters(List(param).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/AdditionalProps"))
  }

  test("PathItem with GET operation containing ApiResponse with content with MediaType with schema with oneOf with $ref") {
    val s1 = new Schema[Object]()
    s1.set$ref("#/components/schemas/OneOfSchema")
    val schema = new Schema[Object]()
    schema.setOneOf(List(s1).asJava.asInstanceOf[java.util.List[Schema[_]]])
    val mt = new MediaType()
    mt.setSchema(schema)
    val content = new Content()
    content.addMediaType("application/json", mt)
    val response = new ApiResponse()
    response.setContent(content)
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/OneOfSchema"))
  }

  test("PathItem with GET operation containing Parameter with content with MediaType with schema with anyOf with $ref") {
    val s1 = new Schema[Object]()
    s1.set$ref("#/components/schemas/AnyOfSchema")
    val schema = new Schema[Object]()
    schema.setAnyOf(List(s1).asJava.asInstanceOf[java.util.List[Schema[_]]])
    val mt = new MediaType()
    mt.setSchema(schema)
    val content = new Content()
    content.addMediaType("application/json", mt)
    val param = new Parameter()
    param.setContent(content)
    val op = new Operation()
    op.setParameters(List(param).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/AnyOfSchema"))
  }

  test("PathItem with GET operation containing Components reference via RequestBody $ref") {
    val rb = new RequestBody()
    rb.set$ref("#/components/requestBodies/CommonBody")
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/requestBodies/CommonBody"))
  }

  test("PathItem with GET operation containing Schema with allOf, oneOf, not, items, additionalProperties with $ref") {
    val s1 = new Schema[Object]()
    s1.set$ref("#/components/schemas/A")
    val s2 = new Schema[Object]()
    s2.set$ref("#/components/schemas/B")
    val s3 = new Schema[Object]()
    s3.set$ref("#/components/schemas/C")
    val s4 = new Schema[Object]()
    s4.set$ref("#/components/schemas/D")
    val s5 = new Schema[Object]()
    s5.set$ref("#/components/schemas/E")

    val schema = new Schema[Object]()
    schema.setAllOf(List(s1).asJava.asInstanceOf[java.util.List[Schema[_]]])
    schema.setOneOf(List(s2).asJava.asInstanceOf[java.util.List[Schema[_]]])
    schema.setNot(s3)
    schema.setItems(s4)
    schema.setAdditionalProperties(s5)

    val param = new Parameter()
    param.setSchema(schema)
    val op = new Operation()
    op.setParameters(List(param).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set(
      "#/components/schemas/A",
      "#/components/schemas/B",
      "#/components/schemas/C",
      "#/components/schemas/D",
      "#/components/schemas/E"
    ))
  }

  test("PathItem with GET operation containing Header with content with MediaType with schema $ref") {
    val schema = new Schema[Object]()
    schema.set$ref("#/components/schemas/HeaderSchema")
    val mt = new MediaType()
    mt.setSchema(schema)
    val content = new Content()
    content.addMediaType("application/json", mt)
    val header = new Header()
    header.setContent(content)
    val response = new ApiResponse()
    response.setHeaders(Map("h" -> header).asJava)
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/HeaderSchema"))
  }

  test("PathItem with GET operation containing Link with operationRef and parameters") {
    val link = new Link()
    link.setOperationRef("#/components/links/UserLink")
    link.setParameters(Map("userId" -> "$request.path.id").asJava)
    val response = new ApiResponse()
    response.setLinks(Map("user" -> link).asJava)
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/links/UserLink"))
  }

  test("PathItem with GET operation containing RequestBody with MediaType with example with $ref") {
    val example = new Example()
    example.set$ref("#/components/examples/BodyExample")
    val mt = new MediaType()
    mt.setExamples(Map("ex" -> example).asJava)
    val content = new Content()
    content.addMediaType("application/json", mt)
    val rb = new RequestBody()
    rb.setContent(content)
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/examples/BodyExample"))
  }

  test("PathItem with GET operation containing RequestBody with MediaType with Encoding with Header with schema $ref") {
    val schema = new Schema[Object]()
    schema.set$ref("#/components/schemas/EncHeaderSchema")
    val header = new Header()
    header.setSchema(schema)
    val encoding = new Encoding()
    encoding.setHeaders(Map("h" -> header).asJava)
    val mt = new MediaType()
    mt.setEncoding(Map("enc" -> encoding).asJava)
    val content = new Content()
    content.addMediaType("application/json", mt)
    val rb = new RequestBody()
    rb.setContent(content)
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/EncHeaderSchema"))
  }

  test("PathItem with GET operation containing Parameter with schema with nested properties with $ref") {
    val nested = new Schema[Object]()
    nested.set$ref("#/components/schemas/NestedProp")
    val schema = new Schema[Object]()
    schema.setProperties(Map("nested" -> nested).asJava.asInstanceOf[java.util.Map[String, Schema[_]]])
    val param = new Parameter()
    param.setSchema(schema)
    val op = new Operation()
    op.setParameters(List(param).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/NestedProp"))
  }

  test("PathItem with GET operation containing ApiResponse with Header with schema with allOf with $ref") {
    val s1 = new Schema[Object]()
    s1.set$ref("#/components/schemas/H1")
    val s2 = new Schema[Object]()
    s2.set$ref("#/components/schemas/H2")
    val allOf = new Schema[Object]()
    allOf.setAllOf(List(s1, s2).asJava.asInstanceOf[java.util.List[Schema[_]]])
    val header = new Header()
    header.setSchema(allOf)
    val response = new ApiResponse()
    response.setHeaders(Map("h" -> header).asJava)
    val responses = new ApiResponses()
    responses.addApiResponse("200", response)
    val op = new Operation()
    op.setResponses(responses)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/H1", "#/components/schemas/H2"))
  }

  test("PathItem with GET operation containing Parameter with content with MediaType with schema with not with $ref") {
    val notSchema = new Schema[Object]()
    notSchema.set$ref("#/components/schemas/NotParam")
    val schema = new Schema[Object]()
    schema.setNot(notSchema)
    val mt = new MediaType()
    mt.setSchema(schema)
    val content = new Content()
    content.addMediaType("application/json", mt)
    val param = new Parameter()
    param.setContent(content)
    val op = new Operation()
    op.setParameters(List(param).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/NotParam"))
  }

  test("PathItem with GET operation containing RequestBody with MediaType with schema with additionalProperties with $ref") {
    val ap = new Schema[Object]()
    ap.set$ref("#/components/schemas/ExtraProps")
    val schema = new Schema[Object]()
    schema.setAdditionalProperties(ap)
    val mt = new MediaType()
    mt.setSchema(schema)
    val content = new Content()
    content.addMediaType("application/json", mt)
    val rb = new RequestBody()
    rb.setContent(content)
    val op = new Operation()
    op.setRequestBody(rb)
    val pathItem = new PathItem()
    pathItem.setGet(op)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/ExtraProps"))
  }

  test("Recursive chain") {
    val schemaC = new Schema[Object]()
    schemaC.set$ref("#/components/schemas/C")

    val schemaB = new Schema[Object]()
    schemaB.set$ref("#/components/schemas/B")
    schemaB.setItems(schemaC)

    val schemaA = new Schema[Object]()
    schemaA.set$ref("#/components/schemas/A")
    schemaA.setProperties(Map("b" -> schemaB).asJava.asInstanceOf[java.util.Map[String, Schema[_]]])

    val param = new Parameter()
    param.setSchema(schemaA)
    val op = new Operation()
    op.setParameters(List(param).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)

    //val refs = generator.collectRefsWithIdentityTracking(pathItem)
    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/A", "#/components/schemas/B", "#/components/schemas/C"))
  }

  test("Schema with discriminator and mapping $ref") {
    val schema = new Schema[Object]()
    schema.setDiscriminator(
      new Discriminator()
        .propertyName("type")
        .mapping(Map("dog" -> "#/components/schemas/Dog").asJava)
    )
    schema.set$ref("#/components/schemas/Animal")

    val param = new Parameter()
    param.setSchema(schema)

    val op = new Operation()
    op.setParameters(List(param).asJava)

    val pathItem = new PathItem()
    pathItem.setGet(op)

    val animalSchema = new Schema[Any]()
    animalSchema.setType("object")
    animalSchema.setProperties(Map[String, Schema[_]]("type" -> new Schema[String]()).asJava)



    val dogSchema = new Schema[Any]()
    dogSchema.setType("object")
    dogSchema.setProperties(Map[String, Schema[_]]("breed" -> new Schema[String]()).asJava)

    val gen1 = new WabaseSwaggerGenerator(
      qes = Seq.empty,
      hostString = "localhost",
      isRelevantView = (_: ViewDef) => true,
      isRelevantRoute = (_: RouteDef) => true
    ) {
      override def components: Option[io.swagger.v3.oas.models.Components] = {
        val c = new io.swagger.v3.oas.models.Components()
        val schemas: Map[String, Schema[_]] = Map(
          "Animal" -> animalSchema,
          "Dog" -> dogSchema
        )
        c.setSchemas(schemas.asJava)
        Some(c)
      }
    }

    val refs = gen1.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/Animal", "#/components/schemas/Dog"))
  }


  test("Schema with external $ref") {
    val schema = new Schema[Object]()
    schema.set$ref("https://example.com/schemas/ExternalSchema.json")

    val param = new Parameter()
    param.setSchema(schema)
    val op = new Operation()
    op.setParameters(List(param).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)

    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("https://example.com/schemas/ExternalSchema.json"))
  }

  test("Schema with circular ref") {
    val schemaA = new Schema[Object]()
    schemaA.set$ref("#/components/schemas/A")

    val schemaB = new Schema[Object]()
    schemaB.set$ref("#/components/schemas/B")
    schemaB.setItems(schemaA)

    schemaA.setItems(schemaB)

    val param = new Parameter()
    param.setSchema(schemaA)
    val op = new Operation()
    op.setParameters(List(param).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)

    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/A", "#/components/schemas/B"))
  }

  test("Schema with deeply nested allOf and oneOf with $ref") {
    val s1 = new Schema[Object]()
    s1.set$ref("#/components/schemas/S1")
    val s2 = new Schema[Object]()
    s2.set$ref("#/components/schemas/S2")
    val s3 = new Schema[Object]()
    s3.set$ref("#/components/schemas/S3")

    val nested = new Schema[Object]()
    nested.setAllOf(List(s1, s2).asJava.asInstanceOf[java.util.List[Schema[_]]])
    nested.setOneOf(List(s3).asJava.asInstanceOf[java.util.List[Schema[_]]])

    val param = new Parameter()
    param.setSchema(nested)
    val op = new Operation()
    op.setParameters(List(param).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)

    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/S1", "#/components/schemas/S2", "#/components/schemas/S3"))
  }

  test("Schema with mixed inline and $ref properties") {
    val refSchema = new Schema[Object]()
    refSchema.set$ref("#/components/schemas/RefProp")

    val inlineSchema = new Schema[Object]()
    inlineSchema.setType("string")

    val schema = new Schema[Object]()
    schema.setProperties(Map(
      "ref" -> refSchema,
      "inline" -> inlineSchema
    ).asJava.asInstanceOf[java.util.Map[String, Schema[_]]])

    val param = new Parameter()
    param.setSchema(schema)
    val op = new Operation()
    op.setParameters(List(param).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)

    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/RefProp"))
  }

  test("PathItem with unused component schema with $ref should not be collected") {
    val unusedSchema = new Schema[Object]()
    unusedSchema.set$ref("#/components/schemas/Unused")

    val components = new Components()
    components.setSchemas(Map("Unused" -> unusedSchema).asJava.asInstanceOf[java.util.Map[String, Schema[_]]])

    val op = new Operation()
    val pathItem = new PathItem()
    pathItem.setGet(op)

    val refs = generator.collectRefs(pathItem)
    assert(refs.isEmpty)
  }


  test("Schema with readOnly, writeOnly, nullable and $ref") {
    val schema = new Schema[Object]()
    schema.set$ref("#/components/schemas/Advanced")
    schema.setReadOnly(true)
    schema.setWriteOnly(false)
    schema.setNullable(true)

    val param = new Parameter()
    param.setSchema(schema)
    val op = new Operation()
    op.setParameters(List(param).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)

    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/Advanced"))
  }

  test("Schema with example and default and $ref") {
    val schema = new Schema[Object]()
    schema.set$ref("#/components/schemas/ExampleSchema")
    schema.setExample("sample")
    schema.setDefault("default")

    val param = new Parameter()
    param.setSchema(schema)
    val op = new Operation()
    op.setParameters(List(param).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)

    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/ExampleSchema"))
  }

  test("Schema with discriminator but no mapping should not collect extra refs") {
    val schema = new Schema[Object]()
    schema.set$ref("#/components/schemas/Base")
    schema.setDiscriminator(new Discriminator().propertyName("type"))

    val param = new Parameter()
    param.setSchema(schema)
    val op = new Operation()
    op.setParameters(List(param).asJava)
    val pathItem = new PathItem()
    pathItem.setGet(op)

    val refs = generator.collectRefs(pathItem)
    assert(refs == Set("#/components/schemas/Base"))
  }

  test("collectRefs should detect recursive $refs between Employee and Manager") {
    // Define Manager schema with reports[] → Employee
    val employeeRef: Schema[_] = {
      val s = new Schema[Any]()
      s.set$ref("#/components/schemas/Employee")
      s
    }

    val arraySchema = new ArraySchema()
    arraySchema.setItems(employeeRef)

    val managerSchema = new Schema[Any]()
    managerSchema.setType("object")
    managerSchema.setProperties(
      Map[String, Schema[_]]("reports" -> arraySchema).asJava
    )

    // Define Employee schema with $ref to Manager
    val managerRef: Schema[_] = {
      val s = new Schema[Any]()
      s.set$ref("#/components/schemas/Manager")
      s
    }

    val employeeSchema = new Schema[Any]()
    employeeSchema.setType("object")
    employeeSchema.setProperties(
      Map[String, Schema[_]]("manager" -> managerRef).asJava
    )

    // Reference Employee schema in response
    val refSchema = new Schema[Any]()
    refSchema.set$ref("#/components/schemas/Employee")

    val mediaType = new MediaType()
    mediaType.setSchema(refSchema)

    val content = new Content()
    content.addMediaType("application/json", mediaType)

    val response = new ApiResponse()
    response.setDescription("Successful response")
    response.setContent(content)

    val responses = new ApiResponses()
    responses.addApiResponse("200", response)

    val operation = new Operation()
    operation.setSummary("Get employee structure")
    operation.setResponses(responses)

    val pathItem = new PathItem()
    pathItem.setGet(operation)

    val paths = new Paths()
    paths.addPathItem("/employee", pathItem)

    val components = new Components()
    val schemas: Map[String, Schema[_]] = Map(
      "Employee" -> employeeSchema,
      "Manager" -> managerSchema
    )
    components.setSchemas(schemas.asJava)
    val info = new Info()
    info.setTitle("Recursive Ref Test")
    info.setVersion("1.0.0")

    val openapi = new OpenAPI()
    openapi.setInfo(info)
    openapi.setPaths(paths)
    openapi.setComponents(components)

    val gen = new WabaseSwaggerGenerator(
      qes = Seq.empty,
      hostString = "localhost",
      isRelevantView = (_: ViewDef) => true,
      isRelevantRoute = (_: RouteDef) => true
    ) {
      override def components: Option[Components] = Option(openapi.getComponents)
      override def info: Info = new Info()
        .description("Recursive Ref Test for unit test")
        .version("1.0.0")
        .title("Recursive Ref Test")
        .termsOfService("")
    }

    val refs = gen.collectRefs(openapi.getPaths.get("/employee"))

    assert(refs.contains("#/components/schemas/Employee"))
    assert(refs.contains("#/components/schemas/Manager"))

  /* test schema YAML for reference
openapi: 3.0.0
info:
  title: Recursive Ref Test
  version: 1.0.0
paths:
  /employee:
    get:
      summary: Get employee structure
      responses:
        '200':
          description: Successful response
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Employee'
components:
  schemas:
    Employee:
      type: object
      properties:
        manager:
          $ref: '#/components/schemas/Manager'
    Manager:
      type: object
      properties:
        reports:
          type: array
          items:
            $ref: '#/components/schemas/Employee'

   */
  }

  test("extractRefs should pass when examples contain non-Example types and parse properly") {
    val mediaType = new MediaType()
    mediaType.setSchema(new Schema().$ref("#/components/schemas/Foo"))

    val mixedExamples = Map(
      "valid" -> new Example().$ref("#/components/examples/ValidExample"),
      "invalidBoolean" -> java.lang.Boolean.TRUE,
      "invalidString" -> "not-an-example"
    ).asJava.asInstanceOf[java.util.Map[String, Example]]

    mediaType.setExamples(mixedExamples)

    val content = new Content()
    content.addMediaType("application/json", mediaType)

    val response = new ApiResponse()
    response.setContent(content)

    val responses = new ApiResponses()
    responses.addApiResponse("200", response)

    val operation = new Operation()
    operation.setResponses(responses)

    val pathItem = new PathItem()
    pathItem.setGet(operation)

    val refs = generator.collectRefs(pathItem)

    assert(refs.contains("#/components/examples/ValidExample"))
  }

  //There is no $ref to extract
  //traversal logic should skip primitives
  test("Boolean in Schema.getAdditionalProperties is valid") {
    val schema = new Schema[Object]()
    schema.setAdditionalProperties(java.lang.Boolean.TRUE)
    val param = new Parameter().schema(schema)
    val pathItem = new PathItem().parameters(List(param).asJava)

    assert(generator.collectRefs(pathItem).isEmpty)
  }

  test("Null Schema.getItems should be treated as absent and not cause failure") {
    val schema = new Schema[Object]()
    schema.setItems(null)
    val param = new Parameter().schema(schema)
    val pathItem = new PathItem().parameters(List(param).asJava)
    val refs = generator.collectRefs(pathItem)
    assert(refs != null && refs.isEmpty)
  }


  test("Non-Example value in MediaType.examples should be ignored and not cause failure") {
    val mt = new MediaType()
    val raw: java.util.Map[String, Object] =
      Map("good" -> new Example().summary("ok"), "bad" -> "not-an-example").asJava
    mt.setExamples(raw.asInstanceOf[java.util.Map[String, Example]])
    val content = new Content().addMediaType("application/json", mt)
    val response = new ApiResponse().content(content)
    val responses = new ApiResponses().addApiResponse("200", response)
    val operation = new Operation().responses(responses)
    val pathItem = new PathItem().get(operation)

    val refs = generator.collectRefs(pathItem)
    assert(refs != null)
    assert(refs.isEmpty)
  }


  test("Non-Header value in ApiResponse.headers should be ignored and not cause failure") {
    val response = new ApiResponse()
    // construct a raw map containing a bad value, then cast only for constructing an intentionally-bad input
    val raw: java.util.Map[String, Object] = Map("good" -> new Header().description("h"), "bad" -> java.lang.Boolean.FALSE).asJava
    response.setHeaders(raw.asInstanceOf[java.util.Map[String, Header]])

    val responses = new ApiResponses().addApiResponse("200", response)
    val operation = new Operation().responses(responses)
    val pathItem = new PathItem().get(operation)

    val refs = generator.collectRefs(pathItem)
    assert(refs != null)
    assert(refs.isEmpty)
  }


  test("Null PathItem.get should be treated as empty and not cause failure") {
    val pathItem = new PathItem().get(null)
    val refs = generator.collectRefs(pathItem)
    assert(refs != null)
    assert(refs.isEmpty)
  }


  test("Empty Operation.getCallbacks should be treated as empty and not cause failure") {
    val operation = new Operation().callbacks(Map.empty[String, Callback].asJava)
    val pathItem = new PathItem().get(operation)
    val refs = generator.collectRefs(pathItem)
    assert(refs != null)
    assert(refs.isEmpty)
  }


  test("Primitive in servers list should be ignored and not cause failure") {
    val pathItem = new PathItem()
    val rawList: java.util.List[AnyRef] = List(new Server().url("https://api.example.com"), java.lang.Boolean.TRUE).asJava
    pathItem.setServers(rawList.asInstanceOf[java.util.List[Server]])
    val refs = generator.collectRefs(pathItem)
    assert(refs != null)
    assert(refs.isEmpty)
  }


  // expect during runtime java.lang.ClassCastException:
  // class java.lang.Boolean cannot be cast to class io.swagger.v3.oas.models.PathItem
  // (java.lang.Boolean is in module java.base of loader 'bootstrap'; io.swagger.v3.oas.models.PathItem is in unnamed module of loader sbt.internal.LayeredClassLoader
  test("Primitive in JMap should cause failure") {
    intercept[ClassCastException] {
      val callback = new Callback()
      callback.put("/cb", java.lang.Boolean.FALSE.asInstanceOf[PathItem])
      val operation = new Operation().callbacks(Map("cb" -> callback).asJava)
      val pathItem = new PathItem().get(operation)
      generator.collectRefs(pathItem)
    }
  }

  test("Null Schema.getProperties should be treated as empty - no failure") {
    val schema = new Schema[Object]()
    schema.setProperties(null)
    val param = new Parameter().schema(schema)
    val pathItem = new PathItem().parameters(List(param).asJava)
    val refs = generator.collectRefs(pathItem)
    assert(refs.isEmpty)
  }


  test("Valid $ref in Link, Header, Example, RequestBody, Parameter should be extracted") {
    val link = new Link().$ref("#/components/links/MyLink")
    val header = new Header().$ref("#/components/headers/MyHeader")
    val example = new Example().$ref("#/components/examples/MyExample")
    val rb = new RequestBody().$ref("#/components/requestBodies/MyRB")
    val param = new Parameter().$ref("#/components/parameters/MyParam")

    val response = new ApiResponse()
    response.setLinks(Map("link" -> link).asJava)
    response.setHeaders(Map("header" -> header).asJava)
    val responses = new ApiResponses().addApiResponse("200", response)

    val operation = new Operation()
    operation.setResponses(responses)
    operation.setRequestBody(rb)
    operation.setParameters(List(param).asJava)

    val mt = new MediaType()
    mt.setExamples(Map("example" -> example).asJava)
    val content = new Content().addMediaType("application/json", mt)
    rb.setContent(content)

    val pathItem = new PathItem().get(operation)
    val refs = generator.collectRefs(pathItem)

    assert(refs.contains("#/components/links/MyLink"))
    assert(refs.contains("#/components/headers/MyHeader"))
    assert(refs.contains("#/components/examples/MyExample"))
    assert(refs.contains("#/components/requestBodies/MyRB"))
    assert(refs.contains("#/components/parameters/MyParam"))
  }

}
