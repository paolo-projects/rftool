//
// Created by paolo on 16/06/22.
//

#ifndef RFTOOL_COLORMAP_H
#define RFTOOL_COLORMAP_H

#include <jni.h>
#include <stdint.h>
#include <cmath>

class ColorMap {
public:
    virtual ~ColorMap() {}
    virtual jint getColor(double intensity) = 0;
};

class GrayscaleColorMap : public ColorMap {
public:
    virtual jint getColor(double intensity) override {
        uint8_t channel = std::max(0, std::min(255, (int) round(intensity * 255)));
        return (channel << 16) | (channel << 8) | channel | (0xFF << 24);
    }
};

class HslColorMap : public ColorMap {
public:
    HslColorMap(double hue0, double hue1, bool reverse) : hue0(hue0), hue1(hue1),
                                                          reverse(reverse) {}

    virtual jint getColor(double intensity) override {
        if (reverse) {
            double hue = std::max(hue1, std::min(hue0, hue0 - intensity * hue0));
            return hslToRgb(hue, 0.5, 1.0);
        } else {
            double hue = std::max(hue0, std::min(hue1, intensity * hue1 - hue0));
            return hslToRgb(hue, 0.5, 1.0);
        }
    }

private:
    double hue0, hue1;
    bool reverse;

protected:
    uint8_t normalizedToUint8(double normalized) {
        return std::max(0.0, std::min(255.0, round(normalized * 255)));
    }

    /**
     * Convert HSL (range 0-360 0-1 0-1) to ARGB int value
     * @param h hue (0-360)
     * @param s saturation (0-1)
     * @param l luminance (0-1)
     * @return The unsigned integer AARRGGBB (alpha is 0xFF)
     */
    jint hslToRgb(double h, double s, double l) {
        double hh, p, q, t, ff;
        long i;
        double r, g, b;

        if (s <= 0.0) {       // < is bogus, just shuts up warnings
            r = l;
            g = l;
            b = l;
        } else {
            hh = h;
            if (hh >= 360.0) hh = 0.0;
            hh /= 60.0;
            i = (long) hh;
            ff = hh - i;
            p = l * (1.0 - s);
            q = l * (1.0 - (s * ff));
            t = l * (1.0 - (s * (1.0 - ff)));

            switch (i) {
                case 0:
                    r = l;
                    g = t;
                    b = p;
                    break;
                case 1:
                    r = q;
                    g = l;
                    b = p;
                    break;
                case 2:
                    r = p;
                    g = l;
                    b = t;
                    break;

                case 3:
                    r = p;
                    g = q;
                    b = l;
                    break;
                case 4:
                    r = t;
                    g = p;
                    b = l;
                    break;
                case 5:
                default:
                    r = l;
                    g = p;
                    b = q;
                    break;
            }
        }
        uint8_t rb = normalizedToUint8(r);
        uint8_t gb = normalizedToUint8(g);
        uint8_t bb = normalizedToUint8(b);

        return (rb << 16) | (gb << 8) | bb | (0xFF << 24);
    }
};

class HeatColorMap : public HslColorMap {
public:
    HeatColorMap() : HslColorMap(62.0, 0.0, true) {}
};

class RainbowColorMap : public HslColorMap {
public:
    RainbowColorMap() : HslColorMap(230.0, 0.0, true) {}
};

#endif //RFTOOL_COLORMAP_H
