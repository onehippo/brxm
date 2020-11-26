/*
 * Copyright 2020 Bloomreach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hippoecm.frontend.plugins.yui.upload.preprocessors;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.hippoecm.frontend.plugins.yui.upload.model.IUploadPreProcessor;
import org.hippoecm.frontend.plugins.yui.upload.model.UploadedFile;

public class TitleFileUploadPreProcessor implements IUploadPreProcessor {
    @Override
    public void process(final UploadedFile uploadedFile) {

        String mimeType = uploadedFile.getContentType();
        PDDocument pdDocument = null;
        if(mimeType.equals("application/pdf")) {
            try {
                File file = uploadedFile.getFile();
                pdDocument = PDDocument.load(file);
                PDDocumentInformation info = pdDocument.getDocumentInformation();
                info.setTitle("Processed by BRXM title");

                pdDocument.save(file);
            } catch (IOException e) {
                // do nothing
            } finally {
                try {
                    if (pdDocument != null) {
                        pdDocument.close();
                    }
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
    }
}
