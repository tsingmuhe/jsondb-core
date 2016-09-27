/*
 * Copyright (c) 2016 Farooq Khan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.jsondb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jsondb.crypto.DefaultAESCipher;
import org.jsondb.crypto.ICipher;
import org.jsondb.JsonDBTemplate;
import org.jsondb.Util;
import org.jsondb.testmodel.Instance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;

/**
 * Unit tests that cover all aspects of DB initialization
 *
 * @author Farooq Khan
 * @version 1.0 31 Dec 2015
 */
public class InitializeDBTests {

  private ObjectMapper objectMapper = null;

  private String dbFilesLocation = "src/test/resources/dbfiles/initializationTests";
  private File dbFilesFolder = new File(dbFilesLocation);
  private File instancesJson = new File(dbFilesFolder, "instances.json");
  private ICipher cipher;

  @Before
  public void setup() throws IOException {
    dbFilesFolder.mkdir();
    Files.copy(new File("src/test/resources/dbfiles/instances.json"), instancesJson);
    try {
      cipher = new DefaultAESCipher("3661d902f427b4da");
    } catch (GeneralSecurityException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    objectMapper = new ObjectMapper();
  }

  @After
  public void tearDown() {
    Util.delete(dbFilesFolder);
  }

  @Test
  public void testInitialization() {
    JsonDBTemplate jsonDBTemplate = new JsonDBTemplate(dbFilesLocation, "org.jsondb.testmodel");

    Set<String> collectionNames = jsonDBTemplate.getCollectionNames();
    assertTrue(collectionNames.contains("instances"));
    assertEquals(collectionNames.size(), 1);
  }

  @Test
  public void testReload() {
    JsonDBTemplate jsonDBTemplate = new JsonDBTemplate(dbFilesLocation, "org.jsondb.testmodel", cipher);

    Set<String> collectionNames = jsonDBTemplate.getCollectionNames();
    assertTrue(collectionNames.contains("instances"));
    //List<Instance> instances = jsonDBTemplate.findAll(Instance.class);
    //int size = instances.size();

    //Add more computers directly to the computers.json file.
    List<Instance> instances1 = new ArrayList<Instance>();
    for (int i = 0; i<10; i++) {
      Instance inst = new Instance();
      int id = 11 + i;
      inst.setId(String.format("%06d", id));
      inst.setHostname("ec2-54-191-" + id);
      inst.setPrivateKey("b87eb02f5dd7e5232d7b0fc30a5015e4");
      inst.setPublicKey("d3aa045f71bf4d1dffd2c5f485a4bc1d");
      instances1.add(inst);
    }
    appendDirectlyToJsonFile(instances1, instancesJson);

    jsonDBTemplate.reLoadDB();

    collectionNames = jsonDBTemplate.getCollectionNames();
    assertTrue(collectionNames.contains("instances"));
    //instances = jsonDBTemplate.findAll(Instance.class);
    //assertEquals(instances.size(), size+10);

  }

  private <T> boolean appendDirectlyToJsonFile(List<T> collectionData, File collectionFile) {

    boolean retval = false;
    FileWriter fw = null;
    try {
      fw = new FileWriter(collectionFile, true);
      for (T row : collectionData) {
        fw.write(objectMapper.writeValueAsString(row));
        fw.write("\n");
      }
      retval = true;
    } catch (IOException e) {
      retval = false;
      e.printStackTrace();
    } finally {
      if (null != fw) {
        try {
          fw.close();
        } catch (IOException e) {
          // do nothing
        }
      }
    }
    return retval;
  }
}
