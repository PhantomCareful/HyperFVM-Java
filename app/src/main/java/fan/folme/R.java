package fan.folme;

/**
 * fan.folme 运行时 R 映射类（自建，替代未随包引入的原 folme 模块 R）。
 *
 * <p>成因：libs/animation-1.0.13.0.aar 的 namespace 为 fan.animation，其内嵌的
 * libs/miuix-animation.jar 是原 folme 模块产物，字节码保留了对 fan.folme.R$id /
 * fan.folme.R$color 的 sget 引用；AGP 只为 aar 生成 fan.animation.R，
 * fan.folme.R 无任何模块提供。运行时 Folme.use() 首次取 folme_tag_animator 即抛
 * NoClassDefFoundError: fan.folme.R$id（下拉菜单动画在 onShown 因此降级静态显示，
 * 背景换色的 Folme 路径同样会触达）。
 *
 * <p>修复：在本模块声明同包名 R 类（proguard 已 keep），字段值映射到本 app 真实
 * 存在的资源（res/values/folme_runtime.xml 自建；nonTransitiveRClass=true 下
 * 自建资源只归 com.careful.HyperFVM.R，运行时必有）。
 * 字段须为非 final，与预编译字节码的 sget 形态一致。
 */
public final class R {
    private R() {
    }

    public static final class id {
        public static int folme_tag_animator =
                com.careful.HyperFVM.R.id.folme_tag_animator;
        public static int miuix_animation_tag_is_dragging =
                com.careful.HyperFVM.R.id.miuix_animation_tag_is_dragging;
        public static int miuix_animation_tag_touch_listener =
                com.careful.HyperFVM.R.id.miuix_animation_tag_touch_listener;
        public static int miuix_animation_tag_hover_set_translation_x =
                com.careful.HyperFVM.R.id.miuix_animation_tag_hover_set_translation_x;

        private id() {
        }
    }

    public static final class color {
        public static int miuix_folme_color_blink_tint =
                com.careful.HyperFVM.R.color.miuix_folme_color_blink_tint;
        public static int miuix_folme_color_touch_tint =
                com.careful.HyperFVM.R.color.miuix_folme_color_touch_tint;
        public static int miuix_folme_color_touch_tint_light =
                com.careful.HyperFVM.R.color.miuix_folme_color_touch_tint_light;
        public static int miuix_folme_color_hover_tint =
                com.careful.HyperFVM.R.color.miuix_folme_color_hover_tint;
        public static int miuix_folme_color_hover_tint_light =
                com.careful.HyperFVM.R.color.miuix_folme_color_hover_tint_light;

        private color() {
        }
    }
}
