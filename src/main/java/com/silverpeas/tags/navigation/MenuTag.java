/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.tags.navigation;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringEscapeUtils;

import com.silverpeas.tags.kmelia.KmeliaTagUtil;
import com.silverpeas.tags.navigation.config.Configurateur;
import com.silverpeas.tags.navigation.links.LinkGeneratorFactory;
import com.silverpeas.tags.navigation.utils.NodeDetailComparator;
import com.silverpeas.tags.pdc.PdcTagUtil;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

/**
 * Tag permettant de générer la structure de l'arborescence du menu.
 * @author svuillet
 */
public class MenuTag extends TagSupport {

  private static final long serialVersionUID = 7316128024807549206L;

  private static final String TOPIC_ID_PREFIX = "topicId-";
  private static final String PARENT_TOPIC_ID_PREFIX = "parentTopicId-";

  private KmeliaTagUtil themetracker = null;
  private PdcTagUtil pdc = new PdcTagUtil(null, null, 0, null);

  private String idTopicRoot;
  private String idTopicSubRoot = null;
  private String selectedTopicIdParameterName = null;
  private String selectedTopicNameInSession = null;
  private String id;
  private String classNamesHierarchy;
  private String maxDeepLevel = null;
  private String excludeTopicsNamed = null;

  private String idAxisFiltering = null;
  private String axisValueFilter = null;
  private String prefixIdHierarchy = null;
  private String classNamesHierarchyFiltered;
  private String classNameSeparator = null;
  private boolean hierarchicSelection = true;

  /**
   * Préfixe la classe css par "selected" de l'item selectionné et eventuellement de ses items
   * parents.
   * @param hierarchicSelection
   */
  public void setHierarchicSelection(String hierarchicSelection) {
    this.hierarchicSelection = new Boolean(hierarchicSelection);
  }

  /**
   * Nom des thèmes qui seront exclus du menu.
   * @param excludeTopicsNamed
   */
  public void setExcludeTopicsNamed(String excludeTopicsNamed) {
    this.excludeTopicsNamed = excludeTopicsNamed;
  }

  /**
   * Prefixes des ids des topics dans l'url générée.
   * @param prefixIdHierarchy
   */
  public void setPrefixIdHierarchy(String prefixIdHierarchy) {
    this.prefixIdHierarchy = prefixIdHierarchy;
  }

  /**
   * Nom de la classe CSS de séparation des items du niveau 1.
   * @param classNameSeparator
   */
  public void setClassNameSeparator(String classNameSeparator) {
    this.classNameSeparator = classNameSeparator;
  }

  /**
   * Id de l'axe qui va servir pour filtrer l'arborescence.
   * @param idAxisFiltering
   */
  public void setIdAxisFiltering(String idAxisFiltering) {
    this.idAxisFiltering = idAxisFiltering;
  }

  /**
   * Valeur du filtre de l'arborescence.
   * @param axisValueFilter
   */
  public void setAxisValueFilter(String axisValueFilter) {
    this.axisValueFilter = axisValueFilter;
  }

  /**
   * Classes CSS alternative aux classes de l'attribut "ClassNamesHierarchy". Ces classes sont
   * appliquées si des éléments du niveau courant sont positionnés sur idAxisFiltering avec la
   * valeurs "axisValueFilter".
   * @param classNamesHierarchyFiltered
   */
  public void setClassNamesHierarchyFiltered(String classNamesHierarchyFiltered) {
    this.classNamesHierarchyFiltered = classNamesHierarchyFiltered;
  }

  /**
   * Point de démarrage du parcours de l'arborescence.
   * @param idTopicRoot
   */
  public void setIdTopicRoot(String idTopicRoot) {
    this.idTopicRoot = idTopicRoot;
  }

  /**
   * Point de démarrage de l'affichage de l'arborescence.
   * @param idTopicSubRoot
   */
  public void setIdTopicSubRoot(String idTopicSubRoot) {
    this.idTopicSubRoot = idTopicSubRoot;
  }

  /**
   * Limite du parcours de l'arborescence.
   * @param maxDeepLevel
   */
  public void setMaxDeepLevel(String maxDeepLevel) {
    this.maxDeepLevel = maxDeepLevel;
  }

  /**
   * Source de données.
   * @param tt
   */
  public void setThemetracker(String tt) {
    int scope = pageContext.getAttributesScope(tt);
    themetracker = (KmeliaTagUtil) pageContext.getAttribute(tt, scope);
  }

  /**
   * Nom du parametre dans l'url qui contient l'id du topic selectionné.
   * @param selectedTopicIdParameterName
   */
  public void setSelectedTopicIdParameterName(String selectedTopicIdParameterName) {
    this.selectedTopicIdParameterName = selectedTopicIdParameterName;
  }

  /**
   * Nom de l'attribut stoqué en session qui contient le topic selectionné.
   * @param selectedTopicNameInSession
   */
  public void setSelectedTopicNameInSession(String selectedTopicNameInSession) {
    this.selectedTopicNameInSession = selectedTopicNameInSession;
  }

  /**
   * Id de l'élément HTML contenant le menu (UL).
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Noms des classes CSS (séparés par des virgules) à appliquer à chaque niveau de l'arborescence.
   * Si le nombre de classes CSS est inférieur à la profondeur de l'arborescence, alors la dernières
   * classes CSS est appliquée au niveau inférieurs.
   * @param classNamesHierarchy
   */
  public void setClassNamesHierarchy(String classNamesHierarchy) {
    this.classNamesHierarchy = classNamesHierarchy;
  }

  /**
   * Retourne un prefix avant l'id d'un élément par rapport à son niveau hierarchique.
   * @param theme
   * @return
   * @throws Exception
   */
  private String getPrefixIdByLevel(NodeDetail theme) throws Exception {
    if (prefixIdHierarchy != null) {
      // selection du bon prefix à appliquer
      int rootLevel =
          KmeliaCaching.getInstance(themetracker).getTopic(Integer.valueOf(idTopicRoot)).getLevel();
      int level = theme.getLevel() - rootLevel;
      StringTokenizer tokenizer = null;
      tokenizer = new StringTokenizer(prefixIdHierarchy, ",");
      int l = 1;
      String prefix = null;
      while (tokenizer.hasMoreTokens()) {
        prefix = tokenizer.nextToken();
        if (level == l) {
          return prefix;
        }
        l++;
      }
    }
    return null;
  }

  /**
   * Retourne le nom de la classe css à appliquer pour un niveau hierarchique donné.
   * @param level
   * @return
   * @throws Exception
   */
  private String getClassNameByLevel(NodeDetail theme) throws Exception {

    boolean useAlternateClassNamesHierarchy = false;
    // gestion du filtrage
    if (idAxisFiltering != null && axisValueFilter != null && !idAxisFiltering.isEmpty() &&
        !axisValueFilter.isEmpty()) {
      @SuppressWarnings("unchecked")
      Collection<PublicationDetail> pubs =
          themetracker.getPublicationsByTopic(String.valueOf(theme.getId()));
      Iterator<PublicationDetail> iPubs = pubs.iterator();
      while (iPubs.hasNext()) {
        PublicationDetail pub = (PublicationDetail) iPubs.next();
        @SuppressWarnings("unchecked")
        Collection<Value> values =
            pdc.getValuesOnAxis(pub.getId() + "," + themetracker.getComponentInst().getId() + "," +
                idAxisFiltering);
        Iterator<Value> iValues = values.iterator();
        while (iValues.hasNext()) {
          Value v = (Value) iValues.next();
          if (v.getName().equals(axisValueFilter)) {
            useAlternateClassNamesHierarchy = true;
            break;
          }
        }
        if (useAlternateClassNamesHierarchy)
          break;
      }
    }

    // selection de la bonne classe css à appliquer
    int rootLevel =
        KmeliaCaching.getInstance(themetracker).getTopic(Integer.valueOf(idTopicRoot)).getLevel();
    int level = theme.getLevel() - rootLevel;
    StringTokenizer tokenizer = null;
    if (useAlternateClassNamesHierarchy) {
      tokenizer = new StringTokenizer(classNamesHierarchyFiltered, ",");
    } else {
      tokenizer = new StringTokenizer(classNamesHierarchy, ",");
    }
    int l = 1;
    String className = null;
    while (tokenizer.hasMoreTokens()) {
      className = tokenizer.nextToken();
      if (level == l) {
        return getFinalClassName(theme, className);
      }
      l++;
    }
    return getFinalClassName(theme, className);
  }

  /**
   * Retourne le nom de la class css à appliquer en fonction de la selection des éléments.
   * @param theme
   * @param className
   * @return
   */
  private String getFinalClassName(NodeDetail theme, String className) throws RemoteException,
      IOException {
    if (isSelectedItem(theme)) {
      if (selectedTopicNameInSession != null) {
        pageContext.getSession().setAttribute(selectedTopicNameInSession, theme);
      }
      return "selected-" + className;
    } else {
      return className;
    }
  }

  /**
   * Test si l'élément du menu est selectionné.
   * @param topicId
   * @return
   */
  private boolean isSelectedItem(NodeDetail theme) throws RemoteException, IOException {
    if (selectedTopicNameInSession != null) {
      NodeDetail node =
          (NodeDetail) pageContext.getSession().getAttribute(selectedTopicNameInSession);
      if (node == null)
        return false;
      String selectedTopicId = String.valueOf(node.getId());
      return isInSelectionPath(selectedTopicId, theme);
    }
    if (selectedTopicIdParameterName != null) {
      String selectedTopicId = pageContext.getRequest().getParameter(selectedTopicIdParameterName);
      if (selectedTopicId != null) {
        return isInSelectionPath(selectedTopicId, theme);
      }
    }
    return false;
  }

  /**
   * Test si un item est dans l'arborescence de selection.
   * @param selectedTopicId
   * @param currentNode
   * @return
   * @throws RemoteException
   */
  private boolean isInSelectionPath(String selectedTopicId, NodeDetail currentNode)
      throws RemoteException {
    if (hierarchicSelection) {
      String selectedTopicsIds =
          KmeliaCaching.getInstance(themetracker).getTopic(Integer.valueOf(selectedTopicId))
              .getFullPath();
      StringTokenizer tokenizer = new StringTokenizer(selectedTopicsIds, "/");
      while (tokenizer.hasMoreTokens()) {
        String nodeId = tokenizer.nextToken();
        if (String.valueOf(currentNode.getId()).equals(nodeId)) {
          return true;
        }
      }
    } else {
      return selectedTopicId.equals(String.valueOf(currentNode.getId()));
    }

    return false;
  }

  /**
   * Construction d'un id html pour un theme.
   * @param theme
   * @return
   */
  private String buildId(String prefix, NodeDetail theme) {
    String genericId = prefix + theme.getId();
    String specificId = Configurateur.getConfigValue(genericId);
    if (specificId != null) {
      return specificId;
    }

    return genericId;
  }

  @Override
  public int doStartTag() throws JspException {
    try {
      JspWriter out = pageContext.getOut();
      NodeDetail root =
          KmeliaCaching.getInstance(themetracker).getTopic(Integer.valueOf(idTopicRoot));

      boolean display = true;
      if (idTopicSubRoot != null) {
        display = idTopicSubRoot.equalsIgnoreCase(idTopicRoot);
      }
      browse(out, root, 1, display);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return SKIP_BODY;
  }

  /**
   * Construction des onglets dans le menu.
   * @param out
   * @param rootTopic
   * @param level
   */
  private void browse(JspWriter out, NodeDetail rootTopic, int level, boolean display) {
    try {
      List<NodeDetail> themes = getSubTopics(themetracker, rootTopic.getId());
      Iterator<NodeDetail> iTheme = themes.iterator();
      if (!themes.isEmpty()) {
        if (level == 1) {
          print(out, "<ul id='" + id + "'>", display);
        } else {
          print(out, "<ul id='" + buildId(PARENT_TOPIC_ID_PREFIX, rootTopic) + "'>", display);
        }
      }
      StringBuffer html = new StringBuffer();
      while (iTheme.hasNext()) {
        NodeDetail theme = (NodeDetail) iTheme.next();
        if (excludeTopicsNamed == null ||
            theme.getName().equalsIgnoreCase(excludeTopicsNamed) == false) {
          html.setLength(0);
          html.append("<li id='");
          html.append(buildId(TOPIC_ID_PREFIX, theme));
          html.append("' class='");
          html.append(getClassNameByLevel(theme));
          html.append("'>");
          html.append("<a href='");
          html.append(generateFullSemanticPath(theme, getPrefixIdByLevel(theme)));
          html.append("' title='");
          html.append(StringEscapeUtils.escapeHtml(theme.getDescription()));
          html.append("'><span>");
          html.append(theme.getName());
          html.append("</span></a>");
          print(out, html.toString(), display);

          if ((maxDeepLevel != null && Integer.valueOf(maxDeepLevel) > level) ||
              maxDeepLevel == null) {
            if (display) {
              browse(out, theme, level + 1, true);
            } else {
              boolean d =
                  idTopicSubRoot.equalsIgnoreCase(String.valueOf(rootTopic.getId())) ||
                      idTopicSubRoot.equalsIgnoreCase(String.valueOf(theme.getId()));
              browse(out, theme, level + 1, d);
            }
          }
          print(out, "</li>", display);
          if (classNameSeparator != null && level == 1 && iTheme.hasNext())
            print(out, "<li class='" + classNameSeparator + "'></li>", display);
        }
      }
      if (!themes.isEmpty()) {
        print(out, "</ul>", display);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Print html.
   * @param out
   * @param html
   * @param display
   */
  private void print(JspWriter out, String html, boolean display) {
    if (display) {
      try {
        out.println(html);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Construction de l'url de l'item.
   * @param node
   * @param prefixId
   * @return
   * @throws RemoteException
   */
  private String generateFullSemanticPath(NodeDetail node, String prefixId) throws RemoteException {
    try {
      return LinkGeneratorFactory.getInstance().newLinkGenerator().generateFullSemanticPath(
          pageContext, themetracker, node, idTopicRoot, null, prefixId);
    } catch (Exception e) {
      throw new RemoteException("", e);
    }
  }

  /**
   * Liste des thèmes au niveau n-1.
   * @param themetracker
   * @param topicId
   * @return
   * @throws RemoteException
   */
  private List<NodeDetail> getSubTopics(KmeliaTagUtil themetracker, int topicId)
      throws RemoteException {

    Collection<NodeDetail> arbo = KmeliaCaching.getInstance(themetracker).getTreeView(idTopicRoot);

    // Calcul du niveau du topic racine
    Iterator<NodeDetail> i = arbo.iterator();
    NodeDetail n = null;
    while (i.hasNext()) {
      n = (NodeDetail) i.next();
      if (n.getId() == Integer.valueOf(topicId)) {
        break;
      }
    }
    int level = n.getLevel() + 1;

    // Constitution de la liste des sous topics
    List<NodeDetail> result = new ArrayList<NodeDetail>();
    i = arbo.iterator();
    while (i.hasNext()) {
      n = (NodeDetail) i.next();
      if (n.getLevel() == level && n.getFatherPK().getId().equals(String.valueOf(topicId))) {
        result.add(n);
      }
    }

    Collections.sort(result, new NodeDetailComparator());
    return result;
  }
}
