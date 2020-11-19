/**
 *    Copyright 2016 FIX Protocol Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.fixprotocol.sbe.transformers;

import io.fixprotocol.orchestra.transformers.RepositoryXslTransformer;
import io.fixprotocol.sbe.validator.SbeSchemaValidator;
import javax.xml.transform.TransformerException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.File;
import java.io.IOException;


public class RepositorySbeTransformerTest {
  
    @Test
    public void transformToSbeV1() throws IOException, TransformerException {
        String[] arr = new String[3];
        arr[0] = Thread.currentThread().getContextClassLoader().getResource("SBE_datatypes.xslt")
                .getFile();
        arr[1] = Thread.currentThread().getContextClassLoader().getResource("trade.xml")
                .getFile();
        // send output to target so it will get cleaned
        arr[2] = "target/test/OrchestraWithSbeDatatypesV1.xml";
        RepositoryXslTransformer.main(arr);
        File outFile = new File(arr[2]);
        assertTrue(outFile.exists());
        

        arr[0] = Thread.currentThread().getContextClassLoader().getResource("OrchestraToSBEV1.xslt")
                .getFile();
        arr[1] = "target/test/OrchestraWithSbeDatatypesV1.xml";
        // send output to target so it will get cleaned
        final String schemaFile = "target/test/SbeSchemaV1.xml";
        arr[2] = schemaFile;
        RepositoryXslTransformer.main(arr);
        outFile = new File(arr[2]);
        assertTrue(outFile.exists());
        
        SbeSchemaValidator validator = SbeSchemaValidator.builder().inputFile(schemaFile)
            .eventLog("target/test/SbeSchemaV1.json").build();
        assertTrue(validator.validate());
    }
    
    @Test
    public void transformToSbeV2() throws IOException, TransformerException {
        String[] arr = new String[3];
        arr[0] = Thread.currentThread().getContextClassLoader().getResource("SBE_datatypes.xslt")
                .getFile();
        arr[1] = Thread.currentThread().getContextClassLoader().getResource("trade.xml")
                .getFile();
        // send output to target so it will get cleaned
        arr[2] = "target/test/OrchestraWithSbeDatatypesV2.xml";
        RepositoryXslTransformer.main(arr);
        File outFile = new File(arr[2]);
        assertTrue(outFile.exists());
        

        arr[0] = Thread.currentThread().getContextClassLoader().getResource("OrchestraToSBEV2.xslt")
                .getFile();
        arr[1] = "target/test/OrchestraWithSbeDatatypesV2.xml";
        // send output to target so it will get cleaned
        final String schemaFile = "target/test/SbeSchemaV2.xml";
        arr[2] = schemaFile;
        RepositoryXslTransformer.main(arr);
        outFile = new File(arr[2]);
        assertTrue(outFile.exists());
        
        SbeSchemaValidator validator = SbeSchemaValidator.builder().inputFile(schemaFile)
            .schemaFile("xsd/sbe-2.0rc3.xsd").eventLog("target/test/SbeSchemaV2.json").build();
        assertTrue(validator.validate());
    }
}
