package net.vladomeme.main.config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class LDPConfig {

    public boolean enabled = true;
    public float minOpacity = 0.2f;
    public float maxOpacity = 1f;
    public double fadeStartRange = 1d;
    public double fadeEndRange = 3d;
    public boolean renderNearbyParticles = false;
    public boolean useInThirdPerson = false;
    public boolean limitEffectRange = false;
    public double effectRange = 10d;

    public transient double fadeStartRangeSquared;
    public transient double fadeEndRangeSquared;
    public transient float opacityLengthMagicNumber;

    private static final File FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "ld-particles.json");
    private static final DecimalFormat decimalFormat = new DecimalFormat("0.##") {{setRoundingMode(RoundingMode.DOWN);}};

    public static final LDPConfig INSTANCE = read();

    public static LDPConfig read() {
        if (!FILE.exists())
            return new LDPConfig().write();

        Reader reader = null;
        try {
            LDPConfig config = new Gson().fromJson(reader = new FileReader(FILE), LDPConfig.class);
            config.onUpdate();
            return config;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            IOUtils.closeQuietly(reader);
        }
    }

    public LDPConfig write() {
        Gson gson = new Gson();
        JsonWriter writer = null;
        try {
            writer = gson.newJsonWriter(new FileWriter(FILE));
            writer.setIndent("    ");
            gson.toJson(gson.toJsonTree(this, LDPConfig.class), writer);
            onUpdate();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            IOUtils.closeQuietly(writer);
        }
        return this;
    }

    private void onUpdate() {
        fadeStartRangeSquared = fadeStartRange * fadeStartRange;
        fadeEndRangeSquared = fadeEndRange * fadeEndRange;
        opacityLengthMagicNumber = (float) (1 / ((fadeEndRangeSquared - fadeStartRangeSquared) / (maxOpacity - minOpacity)));
    }

    public Screen create(Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .save(this::write)
                .title(Text.literal("LD Particles"))

                .category(ConfigCategory.createBuilder().name(Text.literal("General"))

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Enable"))
                                .binding(true, () -> enabled, newVal -> enabled = newVal)
                                .controller(TickBoxControllerBuilder::create).build())

                        .option(Option.<Float>createBuilder()
                                .name(Text.literal("Minimal opacity"))
                                .binding(0.2f, () -> minOpacity, newVal -> minOpacity = newVal)
                                .controller(option -> FloatSliderControllerBuilder.create(option)
                                        .range(0f, 1f).step(0.05f).formatValue(value -> Text.of(decimalFormat.format(value))))
                                .build())

                        .option(Option.<Float>createBuilder()
                                .name(Text.literal("Maximum opacity"))
                                .binding(1f, () -> maxOpacity, newVal -> maxOpacity = newVal)
                                .controller(option -> FloatSliderControllerBuilder.create(option)
                                        .range(0f, 1f).step(0.05f).formatValue(value -> Text.of(decimalFormat.format(value))))
                                .build())

                        .option(Option.<Double>createBuilder()
                                .name(Text.literal("Fade start range"))
                                .description(OptionDescription.of(Text.literal(
                                        "Required distance from camera to apply fade (at min opacity).\n" +
                                                "Particles closer than this are prevented from rendering.")))
                                .binding(1d, () -> fadeStartRange,
                                        newVal -> fadeStartRange = (newVal <= fadeEndRange ? newVal : Math.max(0d, fadeEndRange - 0.1d)))
                                .controller(option -> DoubleFieldControllerBuilder.create(option)
                                        .min(0d).formatValue(value -> Text.of(decimalFormat.format(value)))).build())

                        .option(Option.<Double>createBuilder()
                                .name(Text.literal("Fade end range"))
                                .description(OptionDescription.of(Text.literal(
                                        "Particles further than this value are rendered at max opacity.")))
                                .binding(3d, () -> fadeEndRange,
                                        newVal -> fadeEndRange = (newVal >= fadeStartRange ? newVal : fadeStartRange + 0.1d))
                                .controller(option -> DoubleFieldControllerBuilder.create(option)
                                        .min(0d).formatValue(value -> Text.of(decimalFormat.format(value)))).build())

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Render nearby particles"))
                                .description(OptionDescription.of(Text.literal(
                                        "Particles within fade start range are rendered at minimal opacity.")))
                                .binding(false, () -> renderNearbyParticles, newVal -> renderNearbyParticles = newVal)
                                .controller(TickBoxControllerBuilder::create).build())

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Enable in third person"))
                                .binding(false, () -> useInThirdPerson, newVal -> useInThirdPerson = newVal)
                                .controller(TickBoxControllerBuilder::create).build())

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Limit effect range"))
                                .description(OptionDescription.of(Text.literal(
                                        "Particles outside of effect range (lower option) ignore max opacity option.")))
                                .binding(false, () -> limitEffectRange, newVal -> limitEffectRange = newVal)
                                .controller(TickBoxControllerBuilder::create).build())

                        .option(Option.<Double>createBuilder()
                                .name(Text.literal("Effect range"))
                                .description(OptionDescription.of(Text.literal(
                                        "If limit (above option) is enabled, particles outside of this range (planar) would ignore max opacity option.")))
                                .binding(10d, () -> effectRange, newVal -> effectRange = newVal)
                                .controller(option -> DoubleFieldControllerBuilder.create(option)
                                        .min(0d).formatValue(value -> Text.of(decimalFormat.format(value)))).build())
                        .build())
                .build()
                .generateScreen(parent);
    }
}
