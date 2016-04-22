/**
 * Copyright 2013 Huining (Thomas) Feng (tfeng@berkeley.edu)
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
package com.bacoder.parser.java.adapter;

import org.antlr.v4.runtime.tree.TerminalNode;

import com.bacoder.parser.core.Adapters;
import com.bacoder.parser.java.JavaParser;
import com.bacoder.parser.java.JavaParser.PrimitiveTypeContext;
import com.bacoder.parser.java.api.PrimitiveType;
import com.bacoder.parser.java.api.PrimitiveType.Type;

public class PrimitiveTypeAdapter extends JavaAdapter<PrimitiveTypeContext, PrimitiveType>{

  public PrimitiveTypeAdapter(Adapters adapters) {
    super(adapters);
  }

  @Override
  public PrimitiveType adapt(PrimitiveTypeContext context) {
    PrimitiveType primitiveType = createNode(context);

    TerminalNode node = getChild(context, TerminalNode.class);
    if (node != null) {
      switch (node.getSymbol().getType()) {
      case JavaParser.BOOLEAN:
        primitiveType.setType(Type.BOOLEAN);
        break;
      case JavaParser.BYTE:
        primitiveType.setType(Type.BYTE);
        break;
      case JavaParser.CHAR:
        primitiveType.setType(Type.CHAR);
        break;
      case JavaParser.DOUBLE:
        primitiveType.setType(Type.DOUBLE);
        break;
      case JavaParser.FLOAT:
        primitiveType.setType(Type.FLOAT);
        break;
      case JavaParser.INT:
        primitiveType.setType(Type.INT);
        break;
      case JavaParser.LONG:
        primitiveType.setType(Type.LONG);
        break;
      case JavaParser.SHORT:
        primitiveType.setType(Type.SHORT);
        break;
      default:
      }
    }
    return primitiveType;
  }
}

