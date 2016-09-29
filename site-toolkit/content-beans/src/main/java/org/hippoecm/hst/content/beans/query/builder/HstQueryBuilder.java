/*
 *  Copyright 2016 Hippo B.V. (http://www.onehippo.com)
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hippoecm.hst.content.beans.query.builder;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.hippoecm.hst.container.RequestContextProvider;
import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.HstQueryManager;
import org.hippoecm.hst.content.beans.query.exceptions.QueryException;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.repository.util.DateTools;

public abstract class HstQueryBuilder {

    /*
     * Static methods to create a query builder or filter builder.
     */

    public static HstQueryBuilder create() {
        return new DefaultHstQueryBuilder();
    }

    public static HstQueryBuilder create(final HstQueryManager queryManager) {
        return new DefaultHstQueryBuilder(queryManager);
    }

    /**
     * The {@code fieldName} is in general a property name, for example <em>example:title</em>. Depending on the
     * filter method ({@link FilterBuilder#equalTo}, {@link FilterBuilder#notEqualTo}, {@link FilterBuilder#contains}, etc)
     * the {@code fieldName} is limited to certain conditions. For <strong>all</strong> {@link FilterBuilder} constraints
     * methods the fieldName is allowed to be of the form <em>myhippo:title</em> or <em>address/myhippo:street</em> where the
     * latter is a constraint on a child node its property. There is one exception: When you use {@link FilterBuilder#like(String)}
     * or {@link FilterBuilder#notLike(String)}, it is not allowed to do the constrain on a child node property.
     * <p>
     * For the constraint {@link FilterBuilder#contains(String)} and {@link FilterBuilder#notContains(String)} the
     * {@code fieldName} can next to <em>myhippo:title</em> or <em>address/myhippo:street</em> also be equal to
     * "<em>.</em>" meaning a (not)contains filter node scope level is done
     * </p>
     * @param fieldName the {@code fieldName} this filter operates on
     * @return a new FilterBuilder for {@code fieldName}
     */
    public static FilterBuilder filter(String fieldName) {
        FieldFilterBuilder filterBuilder = new FieldFilterBuilder(fieldName);
        return filterBuilder;
    }

    public static FilterBuilder and(FilterBuilder ... filterBuilders) {
        AndFilterBuilder filterBuilder = new AndFilterBuilder(filterBuilders);
        return filterBuilder;
    }

    public static FilterBuilder or(FilterBuilder ... filterBuilders) {
        OrFilterBuilder filterBuilder = new OrFilterBuilder(filterBuilders);
        return filterBuilder;
    }

    /*
     * Members of a builder instance.
     */

    private DateTools.Resolution defaultResolution;
    private List<Node> scopes = new ArrayList<>();
    private List<Node> excludeScopes = new ArrayList<>();

    private boolean includeSubTypes;
    private List<String> primaryNodeTypes;
    private List<Class<? extends HippoBean>> filterBeanTypes;

    private FilterBuilder filterBuilder;
    private List<OrderByConstruct> orderByConstructs;
    private Integer offset;
    private Integer limit;

    protected HstQueryBuilder() {
    }

    public HstQuery build() throws QueryException, RepositoryException {
        final HstRequestContext requestContext = RequestContextProvider.get();
        return build(requestContext.getSession());
    }

    abstract public HstQuery build(final Session session) throws QueryException;

    public HstQueryBuilder includeSubTypes(boolean includeSubTypes) {
        this.includeSubTypes = includeSubTypes;
        return this;
    }

    protected boolean includeSubTypes() {
        return includeSubTypes;
    }

    public HstQueryBuilder primaryNodeTypes(String ... primaryNodeTypeNames) {
        if (primaryNodeTypeNames != null) {
            if (primaryNodeTypes == null) {
                primaryNodeTypes = new ArrayList<>();
            }

            for (String primaryNodeTypeName : primaryNodeTypeNames) {
                primaryNodeTypes.add(primaryNodeTypeName);
            }
        }

        return this;
    }

    protected List<String> primaryNodeTypes() {
        return primaryNodeTypes;
    }

    public HstQueryBuilder filterBeanTypes(Class<? extends HippoBean> ... filterBeanClazzes) {
        if (filterBeanClazzes != null) {
            if (filterBeanTypes == null) {
                filterBeanTypes = new ArrayList<>();
            }

            for (Class<? extends HippoBean> filterBeanClazz : filterBeanClazzes) {
                filterBeanTypes.add(filterBeanClazz);
            }
        }

        return this;
    }

    protected List<Class<? extends HippoBean>> filterBeanTypes() {
        return filterBeanTypes;
    }

    public HstQueryBuilder defaultResolution(final DateTools.Resolution defaultResolution) {
        this.defaultResolution = defaultResolution;
        return this;
    }

    protected DateTools.Resolution defaultResolution() {
        return defaultResolution;
    }

    public HstQueryBuilder scopes(final Node ... scopeNodes) {
        if (scopeNodes != null) {
            for (Node scopeNode : scopeNodes) {
                scopes.add(scopeNode);
                // in case present in 'scopes', remove it from there because now added as exclusion
                excludeScopes.remove(scopeNode);
            }
        }

        return this;
    }

    public HstQueryBuilder scopes(final HippoBean ... scopeBeans) {
        if (scopeBeans != null) {
            for (HippoBean scopeBean : scopeBeans) {
                scopes.add(scopeBean.getNode());
                // in case present in 'scopes', remove it from there because now added as exclusion
                excludeScopes.remove(scopeBean.getNode());
            }
        }

        return this;
    }

    protected List<Node> scopes() {
        return scopes;
    }

    public HstQueryBuilder excludeScopes(final Node ... excludeScopeNodes) {
        if (excludeScopeNodes != null) {
            for (Node excludeScopeNode : excludeScopeNodes) {
                excludeScopes.add(excludeScopeNode);
                // in case present in 'scopes', remove it from there because now added as exclusion
                scopes.remove(excludeScopeNode);
            }
        }

        return this;
    }

    public HstQueryBuilder excludeScopes(final HippoBean ... excludeScopeBeans) {
        if (excludeScopeBeans != null) {
            for (HippoBean excludeScopeBean : excludeScopeBeans) {
                excludeScopes.add(excludeScopeBean.getNode());
                // in case present in 'scopes', remove it from there because now added as exclusion
                scopes.remove(excludeScopeBean.getNode());
            }
        }

        return this;
    }

    protected List<Node> excludeScopes() {
        return excludeScopes;
    }

    public HstQueryBuilder filter(final FilterBuilder filterBuilder) {
        this.filterBuilder = filterBuilder;
        return this;
    }

    protected FilterBuilder filter() {
        return filterBuilder;
    }

    public HstQueryBuilder orderByAscending(final String fieldName) {
        OrderByConstruct orderBy = new OrderByConstruct(fieldName, true);
        addOrderByConstruct(orderBy);
        return this;
    }

    public HstQueryBuilder orderByAscendingCaseInsensitive(final String fieldName) {
        OrderByConstruct orderBy = new OrderByConstruct(fieldName, true).caseSensitive(false);
        addOrderByConstruct(orderBy);
        return this;
    }

    public HstQueryBuilder orderByDescending(final String fieldName) {
        OrderByConstruct orderBy = new OrderByConstruct(fieldName, false);
        addOrderByConstruct(orderBy);
        return this;
    }

    public HstQueryBuilder orderByDescendingCaseInsensitive(final String fieldName) {
        OrderByConstruct orderBy = new OrderByConstruct(fieldName, false).caseSensitive(false);
        addOrderByConstruct(orderBy);
        return this;
    }

    protected List<OrderByConstruct> orderByConstructs() {
        return orderByConstructs;
    }

    public HstQueryBuilder offset(final int offset) {
        this.offset = offset;
        return this;
    }

    public Integer offset() {
        return offset;
    }

    public HstQueryBuilder limit(final int limit) {
        this.limit = limit;
        return this;
    }

    public Integer limit() {
        return limit;
    }

    private void addOrderByConstruct(final OrderByConstruct orderBy) {
        if (orderByConstructs == null) {
            orderByConstructs = new ArrayList<>();
        }

        orderByConstructs.add(orderBy);
    }
}
