package com.criticalrange.integration;

import com.criticalrange.VulkanModExtra;
import com.criticalrange.config.VulkanModExtraConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating VulkanMod-compatible option pages.
 * This replaces the complex reflection-based page creation from the old integration.
 */
public class VulkanModPageFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger("VulkanMod Page Factory");

    /**
     * Create VulkanMod Extra option pages for the event-based integration system
     */
    public static List<Object> createOptionPages() {
        List<Object> pages = new ArrayList<>();

        try {
            // Check if VulkanMod classes are available
            Class<?> optionPageClass = Class.forName("net.vulkanmod.config.option.OptionPage");
            Class<?> switchOptionClass = Class.forName("net.vulkanmod.config.option.SwitchOption");
            Class<?> optionBlockClass = Class.forName("net.vulkanmod.config.gui.OptionBlock");

            if (VulkanModExtra.CONFIG != null) {
                // Create pages for each major category
                pages.add(createAnimationsPage(optionPageClass, switchOptionClass, optionBlockClass));
                pages.add(createParticlesPage(optionPageClass, switchOptionClass, optionBlockClass));
                pages.add(createDetailsPage(optionPageClass, switchOptionClass, optionBlockClass));
                pages.add(createRenderPage(optionPageClass, switchOptionClass, optionBlockClass));
                pages.add(createExtraPage(optionPageClass, switchOptionClass, optionBlockClass));

                LOGGER.info("Created {} VulkanMod Extra option pages", pages.size());
            }

        } catch (ClassNotFoundException e) {
            LOGGER.debug("VulkanMod option classes not available: {}", e.getMessage());
        } catch (Exception e) {
            LOGGER.warn("Error creating VulkanMod Extra option pages", e);
        }

        return pages;
    }

    private static Object createAnimationsPage(Class<?> optionPageClass, Class<?> switchOptionClass, Class<?> optionBlockClass) throws Exception {
        return createBasicPage(optionPageClass, "VulkanMod Extra - Animations", "Animation settings for VulkanMod Extra");
    }

    private static Object createParticlesPage(Class<?> optionPageClass, Class<?> switchOptionClass, Class<?> optionBlockClass) throws Exception {
        return createBasicPage(optionPageClass, "VulkanMod Extra - Particles", "Particle settings for VulkanMod Extra");
    }

    private static Object createDetailsPage(Class<?> optionPageClass, Class<?> switchOptionClass, Class<?> optionBlockClass) throws Exception {
        return createBasicPage(optionPageClass, "VulkanMod Extra - Details", "Detail settings for VulkanMod Extra");
    }

    private static Object createRenderPage(Class<?> optionPageClass, Class<?> switchOptionClass, Class<?> optionBlockClass) throws Exception {
        return createBasicPage(optionPageClass, "VulkanMod Extra - Render", "Render settings for VulkanMod Extra");
    }

    private static Object createExtraPage(Class<?> optionPageClass, Class<?> switchOptionClass, Class<?> optionBlockClass) throws Exception {
        return createBasicPage(optionPageClass, "VulkanMod Extra - Extra", "Extra settings and features");
    }

    private static Object createBasicPage(Class<?> optionPageClass, String title, String description) throws Exception {
        // Create a basic page instance - this is a simplified version
        // The actual option creation would be more complex, but this provides the structure
        var constructor = optionPageClass.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        Object page = constructor.newInstance(title);

        LOGGER.debug("Created VulkanMod option page: {}", title);
        return page;
    }

    /**
     * Check if VulkanMod option classes are available
     */
    public static boolean isVulkanModAvailable() {
        try {
            Class.forName("net.vulkanmod.config.option.OptionPage");
            Class.forName("net.vulkanmod.config.option.SwitchOption");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}