/*
 * Copyright 2013-2015 Alex Lin.
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
package org.opoo.press.impl;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateModel;
import org.apache.commons.io.FileUtils;
import org.opoo.press.Base;
import org.opoo.press.Excerptable;
import org.opoo.press.Site;
import org.opoo.press.SourceEntry;
import org.opoo.press.util.ClassUtils;
import org.opoo.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Alex Lin
 */
public class FreeMarkerRenderer extends AbstractFreeMarkerRenderer{
    private static final Logger log = LoggerFactory.getLogger(FreeMarkerRenderer.class);
    public static final String PROPERTY_PREFIX = "freemarker.";

    private Configuration configuration;
    private Site site;
    private File templateDir;
    private File workingTemplateDir;
    private Map<String,TemplateModel> templateModels;

    //merge(null), recursive
    private String renderMethod;
    private WorkingTemplateHolder workingTemplateHolder;

    private Map<String,Boolean> templatePreparedCache = new HashMap<String, Boolean>();

    public FreeMarkerRenderer(Site site) {
        super();
        this.site = site;
        templateDir = site.getTemplates();
        log.debug("Template directory: " + templateDir.getAbsolutePath());

        //Working directory
        workingTemplateDir = new File( site.getWorking(), "templates");
        PathUtils.checkDir(workingTemplateDir, PathUtils.Strategy.CREATE_IF_NOT_EXISTS);
        log.debug("Working template directory: {}", workingTemplateDir.getAbsolutePath());

        //configuration
        configuration = new Configuration();
        configuration.setObjectWrapper(new DefaultObjectWrapper());
        configuration.setTemplateLoader(buildTemplateLoader(site));

        Locale locale = site.getLocale();
        if(locale != null){
            configuration.setLocale(site.getLocale());
        }

        //Add import i18n messages template.
        //configuration.addAutoImport("i18n", "i18n/messages.ftl");

        initializeAutoImportTemplates(site, configuration);
        initializeAutoIncludeTemplates(site, configuration);

        initializeTemplateModels();


        renderMethod = (String) site.get(PROPERTY_PREFIX + "render_method");

        String str = (String) site.get(PROPERTY_PREFIX + "macro_layout");
        workingTemplateHolder = (str == null || "true".equalsIgnoreCase(str)) ?
                new MacroWorkingTemplateHolder() : new NonMacroWorkingTemplateHolder();
    }

    private void initializeTemplateModels(){
        templateModels = site.getFactory().getPluginManager().getObjectMap(TemplateModel.class);
        Map<String,String> map = (Map<String, String>) site.get(TemplateModel.class.getName());
        if(map != null){
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String name = entry.getKey();
                String className = entry.getValue();
                TemplateModel t = ClassUtils.newInstance(className, site.getClassLoader(), site, site.getConfig());
                log.debug("Create instance: {}", className);
                templateModels.put(name, t);
            }
        }
    }

    private void initializeAutoImportTemplates(Site site, Configuration configuration){
        Map<String,String> autoImportTemplates = (Map<String, String>) site.get(PROPERTY_PREFIX + "auto_import_templates");
        if(autoImportTemplates != null && !autoImportTemplates.isEmpty()){
            for(Map.Entry<String, String> en: autoImportTemplates.entrySet()){
                configuration.addAutoImport(en.getKey(), en.getValue());
                log.debug("Add auto import: " + en.getKey() + " -> " + en.getValue());
            }
        }
    }

    private void initializeAutoIncludeTemplates(Site site, Configuration configuration){
        List<String> autoIncludeTemplates = (List<String>) site.get(PROPERTY_PREFIX + "auto_include_templates");
        if(autoIncludeTemplates != null && !autoIncludeTemplates.isEmpty()){
            for(String template: autoIncludeTemplates){
                configuration.addAutoInclude(template);
                log.debug("Add auto include: " + template);
            }
        }
    }

    private TemplateLoader buildTemplateLoader(Site site){
        try {
            List<TemplateLoader> loaders = new ArrayList<TemplateLoader>();
            loaders.add(new FileTemplateLoader(workingTemplateDir));
            loaders.add(new FileTemplateLoader(templateDir));
            loaders.add(new ClassTemplateLoader(AbstractFreeMarkerRenderer.class, "/org/opoo/press/templates"));

            //template registered by plugins
            List<TemplateLoader> instances = site.getFactory().getPluginManager().getObjectList(TemplateLoader.class);
            if(instances != null && !instances.isEmpty()){
                loaders.addAll(instances);
            }

            TemplateLoader[] loadersArray = loaders.toArray(new TemplateLoader[loaders.size()]);
            return new MultiTemplateLoader(loadersArray);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    protected Configuration getConfiguration(){
        return configuration;
    }

    @Override
    protected void preProcess(Template template, Object rootMap){
        if(templateModels != null && !templateModels.isEmpty()){
            ((Map<String,Object>)rootMap).putAll(templateModels);
        }
    }

    @Override
    public void prepare(){
        templatePreparedCache.clear();
    }

    @Override
    public void render(Base base, Map<String, Object> rootMap) {
        //render methods: merge|recursive, default is merge
        if (renderMethod == null || "merge".equalsIgnoreCase(renderMethod)) {
            renderMergedTemplate(base, rootMap);
        } else if ("recursive".equalsIgnoreCase(renderMethod)) {
            renderRecursive(base, rootMap);
        } else {
            throw new RuntimeException("Unknown render method: " + renderMethod);
        }
    }

    private void renderMergedTemplate(Base base, Map<String, Object> rootMap) {
        String content = base.getContent();
        String layout = base.getLayout();

        boolean isContentRenderRequired = isRenderRequired(site, base);
        boolean isValidLayout = isValidLayout(layout);

        if (isValidLayout) {
            String templateName;
            if(isContentRenderRequired){
                //对模板进行合并
                templateName = workingTemplateHolder.getMergedWorkingTemplate(layout, content, base.getSource().getSourceEntry());
            }else{
                templateName = workingTemplateHolder.getLayoutWorkingTemplate(layout);
                rootMap.put("content", content);
            }

            content = render(templateName, rootMap);
        }else {
            //!isValidLayout && isContentRenderRequired
            if (isContentRenderRequired) {
                content = renderContent(content, rootMap);
            } else {
                //!isValidLayout && !isContentRenderRequired
                //content = content;
                //do nothing
            }
        }
        //set rendered content
        base.setContent(content);

        if(isContentRenderRequired){
            renderExcerpt(base, rootMap);
        }
    }

    private void renderRecursive(Base base, Map<String, Object> rootMap) {
        String content = base.getContent();
        String layout = base.getLayout();

        boolean isContentRenderRequired = isRenderRequired(site, base);
        boolean isValidLayout = isValidLayout(layout);

        if (isContentRenderRequired) {
            content = renderContent(content, rootMap);
        }

        if (isValidLayout) {
            String name = workingTemplateHolder.getLayoutWorkingTemplate(layout);
            rootMap.put("content", content);
            content = render(name, rootMap);
        } else {
            //do nothing
            //content = content;
        }

        base.setContent(content);

        if(isContentRenderRequired){
            renderExcerpt(base, rootMap);
        }
    }

    private void renderExcerpt(Base base, Map<String, Object> rootMap){
        if(base instanceof Excerptable && ((Excerptable) base).isExcerpted()){
            Excerptable o = (Excerptable) base;
            String excerpt = renderContent(o.getExcerpt(), rootMap);
            o.setExcerpt(excerpt);
        }
    }


    static interface WorkingTemplateHolder{
        String getMergedWorkingTemplate(String layout, String content, SourceEntry entry);
        String getLayoutWorkingTemplate(String layout);
    }

    abstract class AbstractWorkingTemplateHolder implements WorkingTemplateHolder{
        @Override
        public String getMergedWorkingTemplate(String layout, String content, SourceEntry entry) {
            String workingTemplateName = entry.getPath() + "/" + entry.getName() + "." + layout + ".ftl";
            prepareWorkingTemplate(workingTemplateName, entry.getFile(), layout, content);
            return workingTemplateName;
        }

        void prepareWorkingTemplate(String workingTemplateName, File sourceFile, String layout, String content){
            if(templatePreparedCache.containsKey(workingTemplateName)){
                //already prepared
                return;
            }

            File workingTemplateFile = new File(workingTemplateDir, workingTemplateName);

            if(workingTemplateFile.exists()
                    && workingTemplateFile.lastModified() >= sourceFile.lastModified()){

                log.debug("Working template exists and is newer than source file: {}", workingTemplateFile);

            }else {
                String template = buildTemplateContent(layout, content);
                try {
                    FileUtils.write(workingTemplateFile, template, "UTF-8");
                    //workingTemplateFile.setLastModified(sourceFile.lastModified());
                    log.debug("Create working template: {}", workingTemplateFile);
                } catch (IOException e) {
                    throw new RuntimeException("Write working template failed: " + workingTemplateFile, e);
                }
            }

            templatePreparedCache.put(workingTemplateName, true);
        }

        String getLayoutFilename(String layout){
            return "_" + layout + ".ftl";
        }

        protected abstract String buildTemplateContent(String layout, String content);
    }

    class NonMacroWorkingTemplateHolder extends AbstractWorkingTemplateHolder{
        @Override
        public String getLayoutWorkingTemplate(String layout) {
            return getLayoutFilename(layout);
        }

        @Override
        protected String buildTemplateContent(String layout, String content) {
            String layoutFilename = getLayoutFilename(layout);
            File layoutFile = new File(templateDir, layoutFilename);
            try {
                String template = FileUtils.readFileToString(layoutFile, "UTF-8");
                return template.replace("${content}", content);
            }catch (Exception e){
                throw new RuntimeException("Read layout file error: " + layoutFile, e);
            }
        }
    }

    class MacroWorkingTemplateHolder extends AbstractWorkingTemplateHolder{
        @Override
        public String getLayoutWorkingTemplate(String layout) {
            String layoutTemplateName = "_" + layout + ".content.ftl";
            //${templateDir}/_page.ftl
            File layoutFile = new File(templateDir, getLayoutFilename(layout));
            prepareWorkingTemplate(layoutTemplateName, layoutFile, layout, "${content}");
            return layoutTemplateName;
        }

        @Override
        protected String buildTemplateContent(String layout, String content) {
            return new StringBuffer()
                    .append("<#include \"/_")
                    .append(layout)
                    .append(".ftl\"><@")
                    .append(layout)
                    .append("Layout>")
                    .append(content)
                    .append("</@")
                    .append(layout)
                    .append("Layout>")
                    .toString();
        }
    }
}
