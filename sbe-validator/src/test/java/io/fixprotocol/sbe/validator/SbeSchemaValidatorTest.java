package io.fixprotocol.sbe.validator;

import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SbeSchemaValidatorTest {

  @BeforeAll
  static void setUpBeforeClass() throws Exception {
    new File("target/test").mkdirs();
  }

  @Test
  void testValidate() {
    String schemaFile = "src/test/resources/SbeSchemaV1.xml";
    String eventFile = "target/test/SbeSchemaV1.json";
    SbeSchemaValidator validator =
        SbeSchemaValidator.builder().inputFile(schemaFile).eventLog(eventFile).build();
    assertTrue(validator.validate());
  }

}
