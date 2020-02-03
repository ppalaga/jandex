/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
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
 */

package org.jboss.jandex.test;

import static junit.framework.Assert.assertEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.junit.Test;

public class CompositeTestCase {

    private static final DotName BASE_NAME = DotName.createSimple("foo.Base");
    private static final DotName OBJECT_NAME = DotName.createSimple("java.lang.Object");
    private static ClassInfo BASE_INFO = ClassInfo.create(BASE_NAME, OBJECT_NAME, (short) 0, new DotName[0], Collections.<DotName, List<AnnotationInstance>>emptyMap(), false);
    private static final DotName BAR_NAME = DotName.createSimple("foo.Bar");
    private static final DotName FOO_NAME = DotName.createSimple("foo.Foo");


    @Test
    public void testComposite() {
        Index barIndex = createIndex(BAR_NAME);
        Index fooIndex = createIndex(FOO_NAME);

        CompositeIndex index = CompositeIndex.create(fooIndex, barIndex);
        List<AnnotationInstance> annotations = index.getAnnotations(DotName.createSimple("foo.BarAnno"));
        int hit = 0;
        for (AnnotationInstance instance : annotations) {
            if (FOO_NAME.equals(((ClassInfo) instance.target()).name())) {
                hit |= 2;
            } else if (BAR_NAME.equals(((ClassInfo) instance.target()).name())) {
                hit |= 1;
            }
        }
        assertEquals(3, hit);

        assertEquals(5, verifyClasses(barIndex.getAllKnownSubclasses(OBJECT_NAME)));
        assertEquals(6, verifyClasses(fooIndex.getAllKnownSubclasses(OBJECT_NAME)));
        assertEquals(7, verifyClasses(index.getAllKnownSubclasses(OBJECT_NAME)));
    }

    private int verifyClasses(Collection<ClassInfo> allKnownSubclasses) {
        int hit;
        hit = 0;
        for (ClassInfo info : allKnownSubclasses) {
            if (BAR_NAME.equals(info.name())) {
                hit |= 1;
            } else if (FOO_NAME.equals(info.name())) {
                hit |= 2;
            } else if (BASE_NAME.equals(info.name())) {
                hit |= 4;
            }
        }
        return hit;
    }

    private Index createIndex(DotName name) {
        Map<DotName,List<AnnotationInstance>> annotations = new TreeMap<DotName, List<AnnotationInstance>>();
        DotName baseName = BASE_NAME;
        ClassInfo classInfo = ClassInfo.create(name, baseName, (short) 0, new DotName[0], annotations, false);
        ClassInfo baseInfo = BASE_INFO;

        AnnotationValue[] values = new AnnotationValue[] {AnnotationValue.createStringValue("blah", "blah")};
        DotName annotationName = DotName.createSimple("foo.BarAnno");
        AnnotationInstance annotation = AnnotationInstance.create(annotationName, classInfo, values);
        annotations.put(annotationName, Collections.singletonList(annotation));

        Map<DotName, List<ClassInfo>> implementors = Collections.emptyMap();
        Map<DotName, ClassInfo> classes = Collections.singletonMap(name, classInfo);
        Map<DotName,List<ClassInfo>> subclasses = new TreeMap<DotName, List<ClassInfo>>();
        subclasses.put(OBJECT_NAME, Collections.singletonList(baseInfo));
        subclasses.put(baseName, Collections.singletonList(classInfo));

        return Index.create(annotations, subclasses, implementors, classes);
    }
}
