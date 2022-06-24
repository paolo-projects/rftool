//
// Created by paolo on 24/06/22.
//

#ifndef RFTOOL_BANDPASS_H
#define RFTOOL_BANDPASS_H

#include "FFTLib.h"
#include <cmath>
#include "ComplexNumber.h"

#define sqr(x) ((x)*(x))

template<typename T>
class BandPass {
public:
    BandPass(int fCenter, int sampleRate, double BW = 0.0066)
            : fCenter((double)fCenter / sampleRate), BW(BW) {
        double cos_2_pi_f = cos(2 * M_PI * this->fCenter);
        R = 1 - 3 * BW;
        K = (1 - 2 * R * cos_2_pi_f + sqr(R)) /
            (2 - 2 * cos_2_pi_f);
        /*
        a0 = 1 - K;
        a1 = 2 * (K - R) * cos_2_pi_f;
        a2 = sqr(R) - K;
        b1 = 2 * R * cos_2_pi_f;
        b2 = -sqr(R);*/

        convCoeffs[0] = 1 - K;
        convCoeffs[1] = 2 * (K - R) * cos_2_pi_f;
        convCoeffs[2] = sqr(R) - K;

        recCoeffs[0] = 0.0;
        recCoeffs[1] = 2 * R * cos_2_pi_f;
        recCoeffs[2] = -sqr(R);
    }

    std::vector<ComplexNumber<T>> filter(const std::vector<ComplexNumber<T>> &data) {
        std::vector<ComplexNumber<T>> result(data.size());
        for (int i = 0; i < data.size(); i++) {
            for (int n = -2; n <= 0; n++) {
                if (i + n >= 0) {
                    result[i] += data[i + n] * convCoeffs[-n] + result[i + n] * recCoeffs[-n];
                }
            }
        }
        return result;
    }

private:
    double fCenter;
    double BW;
    double K, R;
    double convCoeffs[3];
    double recCoeffs[3];
    //double a0, a1, a2, b1, b2;
};


#endif //RFTOOL_BANDPASS_H
