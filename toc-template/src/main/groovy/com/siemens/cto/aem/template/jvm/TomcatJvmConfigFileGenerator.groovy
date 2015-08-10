package com.siemens.cto.aem.template.jvm

import com.siemens.cto.aem.domain.model.jvm.Jvm

import static com.siemens.cto.aem.template.GeneratorUtils.bindDataToTemplate

/**
 * Wrapper that contains methods that generates configuration files for an Apache Web Server
 *
 * Created by Z003BPEJ on 6/20/14.
 */
public class TomcatJvmConfigFileGenerator {

    private TomcatJvmConfigFileGenerator() {}

    /**
     * Generate server.xml content
     * @param templateFileName the template file name
     * @param jvm the jvm for which server.xml to generate
     * @return generated server.xml content
     */
    public static String getServerXml(final String templateFileName,
                                      final Jvm jvm
    ) {
        final binding = [webServerName: jvm.getHostName(),
                         jvms         : [jvm],
                         comments     : "",
                         catalina     : [base: "KATALINA_TEST"]] // TODO <-- get the real catalina.base from properties (OR change the server-xml.tpl to not use this binding)
        return bindDataToTemplate(binding, templateFileName).toString()
    }

}