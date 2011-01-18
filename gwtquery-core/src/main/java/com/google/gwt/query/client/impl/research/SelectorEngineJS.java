/*
 * Copyright 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.query.client.impl.research;

import static com.google.gwt.query.client.GQUtils.eq;
import static com.google.gwt.query.client.GQUtils.truth;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.query.client.GQUtils;
import com.google.gwt.query.client.JSArray;
import com.google.gwt.query.client.Regexp;
import com.google.gwt.query.client.SelectorEngine;
import com.google.gwt.query.client.impl.SelectorEngineImpl;

/**
 * Runtime selector engine implementation with no-XPath/native support based on
 * DOMAssistant.
 */
public class SelectorEngineJS extends SelectorEngineImpl {
  

  /**
   * Internal class.
   */
  protected static class Sequence {

    public int start;

    public int max;

    public int add;

    public int modVal;
  }

  /**
   * Internal class.
   */
  protected static class SplitRule {

    public String tag;

    public String id;

    public String allClasses;

    public String allAttr;

    public String allPseudos;

    public String tagRelation;

    public SplitRule(String tag, String id, String allClasses, String allAttr,
        String allPseudos) {
      this.tag = tag;
      this.id = id;
      this.allClasses = allClasses;
      this.allAttr = allAttr;
      this.allPseudos = allPseudos;
    }

    public SplitRule(String tag, String id, String allClasses, String allAttr,
        String allPseudos, String tagRelation) {
      this.tag = tag;
      this.id = id;
      this.allClasses = allClasses;
      this.allAttr = allAttr;
      this.allPseudos = allPseudos;
      this.tagRelation = tagRelation;
    }
  }

  protected static Sequence getSequence(String expression) {
    int start = 0, add = 2, max = -1, modVal = -1;
    Regexp expressionRegExp = new Regexp(
        "^((odd|even)|([1-9]\\d*)|((([1-9]\\d*)?)n((\\+|\\-)(\\d+))?)|(\\-(([1-9]\\d*)?)n\\+(\\d+)))$");
    JSArray pseudoValue = expressionRegExp.exec(expression);
    if (!truth(pseudoValue)) {
      return null;
    } else {
      if (truth(pseudoValue.getStr(2))) {        // odd or even
        start = (eq(pseudoValue.getStr(2), "odd")) ? 1 : 2;
        modVal = (start == 1) ? 1 : 0;
      } else if (GQUtils
          .truth(pseudoValue.getStr(3))) {        // single digit
        start = Integer.parseInt(pseudoValue.getStr(3), 10);
        add = 0;
        max = start;
      } else if (truth(pseudoValue.getStr(4))) {        // an+b
        add = truth(pseudoValue.getStr(6)) ? Integer
            .parseInt(pseudoValue.getStr(6), 10) : 1;
        start = truth(pseudoValue.getStr(7)) ? Integer.parseInt(
            (pseudoValue.getStr(8).charAt(0) == '+' ? ""
                : pseudoValue.getStr(8)) + pseudoValue.getStr(9), 10) : 0;
        while (start < 1) {
          start += add;
        }
        modVal = (start > add) ? (start - add) % add
            : ((start == add) ? 0 : start);
      } else if (truth(pseudoValue.getStr(10))) {        // -an+b
        add = truth(pseudoValue.getStr(12)) ? Integer
            .parseInt(pseudoValue.getStr(12), 10) : 1;
        start = max = Integer.parseInt(pseudoValue.getStr(13), 10);
        while (start > add) {
          start -= add;
        }
        modVal = (max > add) ? (max - add) % add : ((max == add) ? 0 : max);
      }
    }
    Sequence s = new Sequence();
    s.start = start;
    s.add = add;
    s.max = max;
    s.modVal = modVal;
    return s;
  }

  public static void clearAdded(JSArray a) {
    for (int i = 0, len = a.size(); i < len; i++) {
      clearAdded(a.getNode(i));
    }
  }

  public static native void clearAdded(Node node) /*-{
      node.added = null;
    }-*/;

  public static native NodeList<Element> getElementsByClassName(String clazz,
      Node ctx) /*-{
      return ctx.getElementsByClassName(clazz);
  }-*/;

  public static native boolean isAdded(Node prevRef) /*-{
      return prevRef.added || false;
    }-*/;

  public static native void setAdded(Node prevRef, boolean added) /*-{
      prevRef.added = added;
    }-*/;

  public static native void setSkipTag(JSArray prevElem, boolean skip) /*-{
      prevElem.skipTag = skip;
    }-*/;

  private static String attrToRegExp(String attrVal, String op) {
    if (GQUtils.eq("^", op)) {
      return "^" + attrVal;
    }
    if (GQUtils.eq("$", op)) {
      return attrVal + "$";
    }
    if (GQUtils.eq("*", op)) {
      return attrVal;
    }
    if (GQUtils.eq("|", op)) {
      return "(^" + attrVal + "(\\-\\w+)*$)";
    }
    if (GQUtils.eq("~", op)) {
      return "\\b" + attrVal + "\\b";
    }
    return GQUtils.truth(attrVal) ? "^" + attrVal + "$" : null;
  }

  private static native boolean checked(Node previous) /*-{
    return previous.checked || false;
  }-*/;

  private static void clearChildElms(JSArray prevParents) {
    for (int n = 0, nl = prevParents.size(); n < nl; n++) {
      setHasChildElms(prevParents.getNode(n), false);
    }
  }

  private static native boolean enabled(Node node) /*-{
    return !node.disabled;
  }-*/;

  private static void getDescendantNodes(JSArray matchingElms,
      String nextTagStr, Node prevRef) {
    NodeList<Element> children = getElementsByTagName(nextTagStr, prevRef);
    for (int k = 0, klen = children.getLength(); k < klen; k++) {
      Node child = children.getItem(k);
      if (child.getParentNode() == prevRef) {
        matchingElms.addNode(child);
      }
    }
  }

  private static NodeList<Element> getElementsByTagName(String tag, Node ctx) {
    if (ctx == null) {
      return JavaScriptObject.createArray().cast();
    }
    return ((Element) ctx).getElementsByTagName(tag);
  }

  private static void getGeneralSiblingNodes(JSArray matchingElms,
      JSArray nextTag, Regexp nextRegExp, Node prevRef) {
    while (
        GQUtils.truth((prevRef = SelectorEngine.getNextSibling(prevRef)))
            && !isAdded(prevRef)) {
      if (!GQUtils.truth(nextTag) || nextRegExp
          .test(prevRef.getNodeName())) {
        setAdded(prevRef, true);
        matchingElms.addNode(prevRef);
      }
    }
  }

  private static void getSiblingNodes(JSArray matchingElms, JSArray nextTag,
      Regexp nextRegExp, Node prevRef) {
    while (
        GQUtils.truth(prevRef = SelectorEngine.getNextSibling(prevRef))
            && prevRef.getNodeType() != Node.ELEMENT_NODE) {
    }
    if (GQUtils.truth(prevRef)) {
      if (!GQUtils.truth(nextTag) || nextRegExp
          .test(prevRef.getNodeName())) {
        matchingElms.addNode(prevRef);
      }
    }
  }

  private static native boolean hasChildElms(Node prevParent) /*-{
      return prevParent.childElms || false;
    }-*/;

  private static native boolean isSkipped(JSArray prevElem) /*-{
       return prevElem.skipTag || false;
    }-*/;

  private static native void setHasChildElms(Node prevParent, boolean bool) /*-{
      prevParent.childElms = bool ? bool : null;
    }-*/;

  private static native JSArray subtractArray(JSArray previousMatch,
      JSArray elementsByPseudo) /*-{
      for (var i=0, src1; (src1=arr1[i]); i++) {
        var found = false;
        for (var j=0, src2; (src2=arr2[j]); j++) {
          if (src2 === src1) {
        found = true;
            break;
          }
        }
        if (found) {
          arr1.splice(i--, 1);
        }
      }
      return arr;
    }-*/;

  private Regexp cssSelectorRegExp;

  private Regexp selectorSplitRegExp;

  private Regexp childOrSiblingRefRegExp;

  public SelectorEngineJS() {
    selectorSplitRegExp = new Regexp("[^\\s]+", "g");
    childOrSiblingRefRegExp = new Regexp("^(>|\\+|~)$");
    cssSelectorRegExp = new Regexp(
        "^(\\w+)?(#[\\w\\u00C0-\\uFFFF\\-\\_]+|(\\*))?((\\.[\\w\\u00C0-\\uFFFF\\-_]+)*)?((\\[\\w+(\\^|\\$|\\*|\\||~)?(=[\"']*[\\w\\u00C0-\\uFFFF\\s\\-\\_\\.]+[\"']*)?\\]+)*)?(((:\\w+[\\w\\-]*)(\\((odd|even|\\-?\\d*n?((\\+|\\-)\\d+)?|[\\w\\u00C0-\\uFFFF\\-_]+|((\\w*\\.[\\w\\u00C0-\\uFFFF\\-_]+)*)?|(\\[#?\\w+(\\^|\\$|\\*|\\||~)?=?[\\w\\u00C0-\\uFFFF\\s\\-\\_\\.]+\\]+)|(:\\w+[\\w\\-]*))\\))?)*)?");
  }

  public NodeList<Element> select(String sel, Node ctx) {
    String selectors[] = sel.replace("\\s*(,)\\s*", "$1").split(",");
    boolean identical = false;
    JSArray elm = JSArray.create();
    for (int a = 0, len = selectors.length; a < len; a++) {
      if (a > 0) {
        identical = false;
        for (int b = 0, bl = a; b < bl; b++) {
          if (GQUtils.eq(selectors[a], selectors[b])) {
            identical = true;
            break;
          }
        }
        if (identical) {
          continue;
        }
      }
      String currentRule = selectors[a];
      JSArray cssSelectors = selectorSplitRegExp.match(currentRule);
      JSArray prevElem = JSArray.create(ctx);
      for (int i = 0, slen = cssSelectors.size(); i < slen; i++) {
        JSArray matchingElms = JSArray.create();
        String rule = cssSelectors.getStr(i);
        if (i > 0 && childOrSiblingRefRegExp.test(rule)) {
          JSArray childOrSiblingRef = childOrSiblingRefRegExp.exec(rule);
          if (GQUtils.truth(childOrSiblingRef)) {
            JSArray nextTag = new Regexp("^\\w+")
                .exec(cssSelectors.getStr(i + 1));
            Regexp nextRegExp = null;
            String nextTagStr = null;
            if (GQUtils.truth(nextTag)) {
              nextTagStr = nextTag.getStr(0);
              nextRegExp = new Regexp("(^|\\s)" + nextTagStr + "(\\s|$)", "i");
            }
            for (int j = 0, jlen = prevElem.size(); j < jlen; j++) {
              Node prevRef = prevElem.getNode(j);
              String ref = childOrSiblingRef.getStr(0);
              if (GQUtils.eq(">", ref)) {
                getDescendantNodes(matchingElms, nextTagStr, prevRef);
              } else if (GQUtils.eq("+", ref)) {
                getSiblingNodes(matchingElms, nextTag, nextRegExp, prevRef);
              } else if (GQUtils.eq("~", ref)) {
                getGeneralSiblingNodes(matchingElms, nextTag, nextRegExp,
                    prevRef);
              }
            }
            prevElem = matchingElms;
            clearAdded(prevElem);
            rule = cssSelectors.getStr(++i);
            if (new Regexp("^\\w+$").test(rule)) {
              continue;
            }
            setSkipTag(prevElem, true);
          }
        }
        JSArray cssSelector = cssSelectorRegExp.exec(rule);
        SplitRule splitRule = new SplitRule(
            !GQUtils.truth(cssSelector.getStr(1)) || GQUtils
                .eq(cssSelector.getStr(3), "*") ? "*" : cssSelector.getStr(1),
            !GQUtils.eq(cssSelector.getStr(3), "*") ? cssSelector
                .getStr(2) : null, cssSelector.getStr(4), cssSelector.getStr(6),
            cssSelector.getStr(10));
        if (GQUtils.truth(splitRule.id)) {
          Element domelem = Document.get()
              .getElementById(splitRule.id.substring(1));
          if (GQUtils.truth(domelem)) {
            matchingElms = JSArray.create(domelem);
          }
          prevElem = matchingElms;
        } else if (GQUtils.truth(splitRule.tag) && !isSkipped(
            prevElem)) {
          if (i == 0 && matchingElms.size() == 0 && prevElem.size() == 1) {
            prevElem = matchingElms = JSArray.create(
                getElementsByTagName(splitRule.tag, prevElem.getNode(0)));
          } else {
            NodeList<Element> tagCollectionMatches;
            for (int l = 0, ll = prevElem.size(); l < ll; l++) {
              tagCollectionMatches = getElementsByTagName(splitRule.tag,
                  prevElem.getNode(l));
              for (int m = 0, mlen = tagCollectionMatches.getLength(); m < mlen;
                  m++) {
                Node tagMatch = tagCollectionMatches.getItem(m);

                if (!isAdded(tagMatch)) {
                  setAdded(tagMatch, true);
                  matchingElms.addNode(tagMatch);
                }
              }
            }
            prevElem = matchingElms;
            clearAdded(prevElem);
          }
          if (matchingElms.size() == 0) {
            break;
          }
          setSkipTag(prevElem, false);
          if (GQUtils.truth(splitRule.allClasses)) {
            String[] allClasses = splitRule.allClasses.replaceFirst("^\\.", "")
                .split("\\.");
            Regexp[] regExpClassNames = new Regexp[allClasses.length];
            for (int n = 0, nl = allClasses.length; n < nl; n++) {
              regExpClassNames[n] = new Regexp(
                  "(^|\\s)" + allClasses[n] + "(\\s|$)");
            }
            JSArray matchingClassElms = JSArray.create();
            for (int o = 0, olen = prevElem.size(); o < olen; o++) {
              Element current = prevElem.getElement(o);
              String elmClass = current.getClassName();
              boolean addElm = false;
              if (GQUtils.truth(elmClass) && !isAdded(current)) {
                for (int p = 0, pl = regExpClassNames.length; p < pl; p++) {
                  addElm = regExpClassNames[p].test(elmClass);
                  if (!addElm) {
                    break;
                  }
                }
                if (addElm) {
                  setAdded(current, true);
                  matchingClassElms.addNode(current);
                }
              }
            }
            clearAdded(prevElem);
            prevElem = matchingElms = matchingClassElms;
          }
          if (GQUtils.truth(splitRule.allAttr)) {
            JSArray allAttr = Regexp
                .match("\\[[^\\]]+\\]", "g", splitRule.allAttr);
            Regexp[] regExpAttributes = new Regexp[allAttr.size()];
            String[] regExpAttributesStr = new String[allAttr.size()];
            Regexp attributeMatchRegExp = new Regexp(
                "(\\w+)(\\^|\\$|\\*|\\||~)?=?[\"']?([\\w\u00C0-\uFFFF\\s\\-_\\.]+)?");
            for (int q = 0, ql = allAttr.size(); q < ql; q++) {
              JSArray attributeMatch = attributeMatchRegExp
                  .exec(allAttr.getStr(q));
              String attributeValue =
                  GQUtils.truth(attributeMatch.getStr(3))
                      ? attributeMatch.getStr(3).replaceAll("\\.", "\\.")
                      : null;
              String attrVal = attrToRegExp(attributeValue,
                  (GQUtils.or(attributeMatch.getStr(2), null)));
              regExpAttributes[q] = (GQUtils.truth(attrVal) ? new Regexp(
                  attrVal) : null);
              regExpAttributesStr[q] = attributeMatch.getStr(1);
            }
            JSArray matchingAttributeElms = JSArray.create();

            for (int r = 0, rlen = matchingElms.size(); r < rlen; r++) {
              Element current = matchingElms.getElement(r);
              boolean addElm = false;
              for (int s = 0, sl = regExpAttributes.length;
                  s < sl; s++) {
                addElm = false;
                Regexp attributeRegexp = regExpAttributes[s];
                String currentAttr = getAttr(current, regExpAttributesStr[s]);
                if (GQUtils.truth(currentAttr)
                    && currentAttr.length() != 0) {
                  if (attributeRegexp == null || attributeRegexp
                      .test(currentAttr)) {
                    addElm = true;
                  }
                }
                if (!addElm) {
                  break;
                }
              }
              if (addElm) {
                matchingAttributeElms.addNode(current);
              }
            }
            prevElem = matchingElms = matchingAttributeElms;
          }
          if (GQUtils.truth(splitRule.allPseudos)) {
            Regexp pseudoSplitRegExp = new Regexp(
                ":(\\w[\\w\\-]*)(\\(([^\\)]+)\\))?");

            JSArray allPseudos = Regexp
                .match("(:\\w+[\\w\\-]*)(\\([^\\)]+\\))?", "g",
                    splitRule.allPseudos);
            for (int t = 0, tl = allPseudos.size(); t < tl; t++) {
              JSArray pseudo = pseudoSplitRegExp.match(allPseudos.getStr(t));
              String pseudoClass = GQUtils.truth(pseudo.getStr(1))
                  ? pseudo.getStr(1).toLowerCase() : null;
              String pseudoValue = GQUtils.truth(pseudo.getStr(3))
                  ? pseudo.getStr(3) : null;
              matchingElms = getElementsByPseudo(matchingElms, pseudoClass,
                  pseudoValue);
              clearAdded(matchingElms);
            }
            prevElem = matchingElms;
          }
        }
      }
      elm.pushAll(prevElem);
    }

    return GQUtils.unique(elm.<JsArray<Element>>cast()).cast();
  }

  protected String getAttr(Element current, String name) {
    return current.getAttribute(name);
  }

  private void getCheckedPseudo(JSArray previousMatch, JSArray matchingElms) {
    Node previous;
    for (int q = 0, qlen = previousMatch.size(); q < qlen; q++) {
      previous = previousMatch.getNode(q);
      if (checked(previous)) {
        matchingElms.addNode(previous);
      }
    }
  }

  private void getContainsPseudo(JSArray previousMatch, String pseudoValue,
      JSArray matchingElms) {
    Node previous;
    for (int q = 0, qlen = previousMatch.size(); q < qlen; q++) {
      previous = previousMatch.getNode(q);
      if (!isAdded(previous)) {
        if (((Element) previous).getInnerText().indexOf(pseudoValue) != -1) {
          setAdded(previous, true);
          matchingElms.addNode(previous);
        }
      }
    }
  }

  private void getDefaultPseudo(JSArray previousMatch, String pseudoClass,
      String pseudoValue, JSArray matchingElms) {
    Node previous;
    for (int w = 0, wlen = previousMatch.size(); w < wlen; w++) {
      previous = previousMatch.getElement(w);
      if (GQUtils
          .eq(((Element) previous).getAttribute(pseudoClass), pseudoValue)) {
        matchingElms.addNode(previous);
      }
    }
  }

  private void getDisabledPseudo(JSArray previousMatch, JSArray matchingElms) {
    Node previous;
    for (int q = 0, qlen = previousMatch.size(); q < qlen; q++) {
      previous = previousMatch.getNode(q);
      if (!enabled(previous)) {
        matchingElms.addNode(previous);
      }
    }
  }

  private JSArray getElementsByPseudo(JSArray previousMatch, String pseudoClass,
      String pseudoValue) {
    JSArray prevParents = JSArray.create();
    boolean previousDir = pseudoClass.startsWith("first") ? true : false;
    JSArray matchingElms = JSArray.create();
    if (GQUtils.eq("first-child", pseudoClass) || GQUtils
        .eq("last-child", pseudoClass)) {
      getFirstChildPseudo(previousMatch, previousDir, matchingElms);
    } else if (GQUtils.eq("only-child", pseudoClass)) {
      getOnlyChildPseudo(previousMatch, matchingElms);
    } else if (GQUtils.eq("nth-child", pseudoClass)) {
      matchingElms = getNthChildPseudo(previousMatch, pseudoValue, prevParents,
          matchingElms);
    } else if (GQUtils.eq("first-of-type", pseudoClass) || GQUtils
        .eq("last-of-type", pseudoClass)) {
      getFirstOfTypePseudo(previousMatch, previousDir, matchingElms);
    } else if (GQUtils.eq("only-of-type", pseudoClass)) {
      getOnlyOfTypePseudo(previousMatch, matchingElms);
    } else if (GQUtils.eq("nth-of-type", pseudoClass)) {
      matchingElms = getNthOfTypePseudo(previousMatch, pseudoValue, prevParents,
          matchingElms);
    } else if (GQUtils.eq("empty", pseudoClass)) {
      getEmptyPseudo(previousMatch, matchingElms);
    } else if (GQUtils.eq("enabled", pseudoClass)) {
      getEnabledPseudo(previousMatch, matchingElms);
    } else if (GQUtils.eq("disabled", pseudoClass)) {
      getDisabledPseudo(previousMatch, matchingElms);
    } else if (GQUtils.eq("checked", pseudoClass)) {
      getCheckedPseudo(previousMatch, matchingElms);
    } else if (GQUtils.eq("contains", pseudoClass)) {
      getContainsPseudo(previousMatch, pseudoValue, matchingElms);
    } else if (GQUtils.eq("not", pseudoClass)) {
      matchingElms = getNotPseudo(previousMatch, pseudoValue, matchingElms);
    } else {
      getDefaultPseudo(previousMatch, pseudoClass, pseudoValue, matchingElms);
    }
    return matchingElms;
  }

  private void getEmptyPseudo(JSArray previousMatch, JSArray matchingElms) {
    Node previous;
    for (int q = 0, qlen = previousMatch.size(); q < qlen; q++) {
      previous = previousMatch.getNode(q);
      if (!previous.hasChildNodes()) {
        matchingElms.addNode(previous);
      }
    }
  }

  private void getEnabledPseudo(JSArray previousMatch, JSArray matchingElms) {
    Node previous;
    for (int q = 0, qlen = previousMatch.size(); q < qlen; q++) {
      previous = previousMatch.getNode(q);
      if (enabled(previous)) {
        matchingElms.addNode(previous);
      }
    }
  }

  private void getFirstChildPseudo(JSArray previousMatch, boolean previousDir,
      JSArray matchingElms) {
    Node next;
    Node previous;
    for (int j = 0, jlen = previousMatch.size(); j < jlen; j++) {
      previous = next = previousMatch.getElement(j);
      if (previousDir) {
        while (GQUtils
            .truth((next = SelectorEngine.getPreviousSibling(next)))
            && next.getNodeType() != Node.ELEMENT_NODE) {
        }
      } else {
        while (
            GQUtils.truth((next = SelectorEngine.getNextSibling(next)))
                && next.getNodeType() != Node.ELEMENT_NODE) {
        }
      }
      if (!GQUtils.truth(next)) {
        matchingElms.addNode(previous);
      }
    }
  }

  private void getFirstOfTypePseudo(JSArray previousMatch, boolean previousDir,
      JSArray matchingElms) {
    Node previous;
    Node next;
    for (int n = 0, nlen = previousMatch.size(); n < nlen; n++) {
      next = previous = previousMatch.getNode(n);

      if (previousDir) {
        while (
            GQUtils.truth(next = SelectorEngine.getPreviousSibling(next))
                && !GQUtils
                .eq(next.getNodeName(), previous.getNodeName())) {
        }
      } else {
        while (GQUtils.truth(next = SelectorEngine.getNextSibling(next))
            && !GQUtils.eq(next.getNodeName(), previous.getNodeName())) {
        }
      }

      if (!GQUtils.truth(next)) {
        matchingElms.addNode(previous);
      }
    }
  }

  private JSArray getNotPseudo(JSArray previousMatch, String pseudoValue,
      JSArray matchingElms) {
    if (new Regexp("(:\\w+[\\w\\-]*)$").test(pseudoValue)) {
      matchingElms = subtractArray(previousMatch,
          getElementsByPseudo(previousMatch, pseudoValue.substring(1), ""));
    } else {
      pseudoValue = pseudoValue
          .replace("^\\[#([\\w\\u00C0-\\uFFFF\\-\\_]+)\\]$", "[id=$1]");
      JSArray notTag = new Regexp("^(\\w+)").exec(pseudoValue);
      JSArray notClass = new Regexp("^\\.([\\w\u00C0-\uFFFF\\-_]+)")
          .exec(pseudoValue);
      JSArray notAttr = new Regexp(
          "\\[(\\w+)(\\^|\\$|\\*|\\||~)?=?([\\w\\u00C0-\\uFFFF\\s\\-_\\.]+)?\\]")
          .exec(pseudoValue);
      Regexp notRegExp = new Regexp("(^|\\s)"
          + (GQUtils.truth(notTag) ? notTag.getStr(1)
          : GQUtils.truth(notClass) ? notClass.getStr(1) : "")
          + "(\\s|$)", "i");
      if (GQUtils.truth(notAttr)) {
        String notAttribute = GQUtils.truth(notAttr.getStr(3)) ? notAttr
            .getStr(3).replace("\\.", "\\.") : null;
        String notMatchingAttrVal = attrToRegExp(notAttribute,
            notAttr.getStr(2));
        notRegExp = new Regexp(notMatchingAttrVal, "i");
      }
      for (int v = 0, vlen = previousMatch.size(); v < vlen; v++) {
        Element notElm = previousMatch.getElement(v);
        Element addElm = null;
        if (GQUtils.truth(notTag) && !notRegExp
            .test(notElm.getNodeName())) {
          addElm = notElm;
        } else if (GQUtils.truth(notClass) && !notRegExp
            .test(notElm.getClassName())) {
          addElm = notElm;
        } else if (GQUtils.truth(notAttr)) {
          String att = getAttr(notElm, notAttr.getStr(1));
          if (!GQUtils.truth(att) || !notRegExp.test(att)) {
            addElm = notElm;
          }
        }
        if (GQUtils.truth(addElm) && !isAdded(addElm)) {
          setAdded(addElm, true);
          matchingElms.addNode(addElm);
        }
      }
    }
    return matchingElms;
  }

  private JSArray getNthChildPseudo(JSArray previousMatch, String pseudoValue,
      JSArray prevParents, JSArray matchingElms) {
    Node previous;
    if (GQUtils.eq(pseudoValue, "n")) {
      matchingElms = previousMatch;
    } else {
      Sequence sequence = getSequence(pseudoValue);
      if (sequence != null) {
        for (int l = 0, llen = previousMatch.size(); l < llen; l++) {
          previous = previousMatch.getNode(l);
          Node prevParent = previous.getParentNode();
          if (!hasChildElms(prevParent)) {
            int iteratorNext = sequence.start;
            int childCount = 0;
            Node childElm = prevParent.getFirstChild();
            while (childElm != null && (sequence.max < 0
                || iteratorNext <= sequence.max)) {
              if (childElm.getNodeType() == Node.ELEMENT_NODE) {
                if (++childCount == iteratorNext) {
                  if (GQUtils
                      .eq(childElm.getNodeName(), previous.getNodeName())) {
                    matchingElms.addNode(childElm);
                  }
                  iteratorNext += sequence.add;
                }
              }
              childElm = SelectorEngine.getNextSibling(childElm);
            }
            setHasChildElms(prevParent, true);
            prevParents.addNode(prevParent);
          }
        }
        clearChildElms(prevParents);
      }
    }
    return matchingElms;
  }

  private JSArray getNthOfTypePseudo(JSArray previousMatch, String pseudoValue,
      JSArray prevParents, JSArray matchingElms) {
    Node previous;
    if (pseudoValue.startsWith("n")) {
      matchingElms = previousMatch;
    } else {
      Sequence sequence = getSequence(pseudoValue);
      if (sequence != null) {
        for (int p = 0, plen = previousMatch.size(); p < plen; p++) {
          previous = previousMatch.getNode(p);
          Node prevParent = previous.getParentNode();
          if (!hasChildElms(prevParent)) {
            int iteratorNext = sequence.start;
            int childCount = 0;
            Node childElm = prevParent.getFirstChild();
            while (GQUtils.truth(childElm) && (sequence.max < 0
                || iteratorNext <= sequence.max)) {
              if (GQUtils
                  .eq(childElm.getNodeName(), previous.getNodeName())) {
                if (++childCount == iteratorNext) {
                  matchingElms.addNode(childElm);
                  iteratorNext += sequence.add;
                }
              }
              childElm = SelectorEngine.getNextSibling(childElm);
            }
            setHasChildElms(prevParent, true);
            prevParents.addNode(prevParent);
          }
        }
        clearChildElms(prevParents);
      }
    }
    return matchingElms;
  }

  private void getOnlyChildPseudo(JSArray previousMatch, JSArray matchingElms) {
    Node previous;
    Node next;
    Node prev;
    Node kParent = null;
    for (int k = 0, klen = previousMatch.size(); k < klen; k++) {
      prev = next = previous = previousMatch.getNode(k);
      Node prevParent = previous.getParentNode();
      if (prevParent != kParent) {
        while (
            GQUtils.truth(prev = SelectorEngine.getPreviousSibling(prev))
                && prev.getNodeType() != Node.ELEMENT_NODE) {
        }
        while (GQUtils.truth(next = SelectorEngine.getNextSibling(next))
            && next.getNodeType() != Node.ELEMENT_NODE) {
        }
        if (!GQUtils.truth(prev) && !GQUtils.truth(next)) {
          matchingElms.addNode(previous);
        }
        kParent = prevParent;
      }
    }
  }

  private void getOnlyOfTypePseudo(JSArray previousMatch,
      JSArray matchingElms) {
    Node previous;
    Node next;
    Node prev;
    Node oParent = null;
    for (int o = 0, olen = previousMatch.size(); o < olen; o++) {
      prev = next = previous = previousMatch.getNode(o);
      Node prevParent = previous.getParentNode();
      if (prevParent != oParent) {
        while (
            GQUtils.truth(prev = SelectorEngine.getPreviousSibling(prev))
                && !GQUtils
                .eq(prev.getNodeName(), previous.getNodeName())) {
        }
        while (GQUtils.truth(next = SelectorEngine.getNextSibling(next))
            && !GQUtils.eq(next.getNodeName(), previous.getNodeName())) {
        }
        if (!GQUtils.truth(prev) && !GQUtils.truth(next)) {
          matchingElms.addNode(previous);
        }
        oParent = prevParent;
      }
    }
  }
}