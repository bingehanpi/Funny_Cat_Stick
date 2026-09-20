package io.github.bingehanpi.funny_cat_stick.registry;

import io.github.bingehanpi.funny_cat_stick.FunnyCatStick;
import io.github.bingehanpi.funny_cat_stick.effect.CatPersonality;
import io.github.bingehanpi.funny_cat_stick.enchantment.CatEnchantmentType;
import io.github.bingehanpi.funny_cat_stick.enchantment.CatPersonalityEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public final class ModEnchantments {

    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, FunnyCatStick.MOD_ID);

    public static final RegistryObject<Enchantment> TOUGH_CATGIRL = register("tough_catgirl", CatPersonality.TOUGH);
    public static final RegistryObject<Enchantment> YANDERE_CATGIRL = register("yandere_catgirl", CatPersonality.YANDERE);
    public static final RegistryObject<Enchantment> TSUNDERE_CATGIRL = register("tsundere_catgirl", CatPersonality.TSUNDERE);
    public static final RegistryObject<Enchantment> ENERGETIC_CATGIRL = register("energetic_catgirl", CatPersonality.ENERGETIC);
    public static final RegistryObject<Enchantment> LAZY_CATGIRL = register("lazy_catgirl", CatPersonality.LAZY);
    public static final RegistryObject<Enchantment> SHARP_TONGUED_CATGIRL = register(
            "sharp_tongued_catgirl",
            CatPersonality.SHARP_TONGUED
    );
    public static final RegistryObject<Enchantment> TIMID_CATGIRL = register("timid_catgirl", CatPersonality.TIMID);
    public static final RegistryObject<Enchantment> AIRHEADED_CATGIRL = register(
            "airheaded_catgirl",
            CatPersonality.AIRHEADED
    );
    public static final RegistryObject<Enchantment> NEKOMATA_DEITY_CATGIRL = register(
            "nekomata_deity_catgirl",
            CatPersonality.NEKOMATA_DEITY
    );
    public static final RegistryObject<Enchantment> IKUN_CATGIRL = register(
            "ikun_catgirl",
            CatPersonality.IKUN,
            CatEnchantmentType.TREASURE
    );

    public static final List<RegistryObject<Enchantment>> NORMAL_PERSONALITY_ENCHANTMENTS = List.of(
            TOUGH_CATGIRL,
            YANDERE_CATGIRL,
            TSUNDERE_CATGIRL,
            ENERGETIC_CATGIRL,
            LAZY_CATGIRL,
            SHARP_TONGUED_CATGIRL,
            TIMID_CATGIRL,
            AIRHEADED_CATGIRL,
            NEKOMATA_DEITY_CATGIRL
    );
    public static final List<RegistryObject<Enchantment>> TREASURE_PERSONALITY_ENCHANTMENTS = List.of(IKUN_CATGIRL);
    public static final List<RegistryObject<Enchantment>> PERSONALITY_ENCHANTMENTS = List.of(
            TOUGH_CATGIRL,
            YANDERE_CATGIRL,
            TSUNDERE_CATGIRL,
            ENERGETIC_CATGIRL,
            LAZY_CATGIRL,
            SHARP_TONGUED_CATGIRL,
            TIMID_CATGIRL,
            AIRHEADED_CATGIRL,
            NEKOMATA_DEITY_CATGIRL,
            IKUN_CATGIRL
    );

    private ModEnchantments() {
    }

    private static RegistryObject<Enchantment> register(String name, CatPersonality personality) {
        return register(name, personality, CatEnchantmentType.NORMAL);
    }

    private static RegistryObject<Enchantment> register(
            String name,
            CatPersonality personality,
            CatEnchantmentType type
    ) {
        return ENCHANTMENTS.register(
                name,
                () -> new CatPersonalityEnchantment(personality, type)
        );
    }

    public static void register(IEventBus modEventBus) {
        ENCHANTMENTS.register(modEventBus);
    }
}
