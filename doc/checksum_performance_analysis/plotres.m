close all;

loadres;

NErrActual = respcs.NErrActual + ressum.NErrActual + rescrc.NErrActual + resflet.NErrActual;
Nsim      = respcs.Nsim + ressum.Nsim + rescrc.Nsim + resflet.Nsim;

figure()
semilogy(respcs.EsN0dB, NErrActual./Nsim), hold on
semilogy(respcs.EsN0dB, respcs.NErrMissed./respcs.Nsim)
semilogy(ressum.EsN0dB, ressum.NErrMissed./ressum.Nsim)
semilogy(rescrc.EsN0dB, rescrc.NErrMissed./rescrc.Nsim)
semilogy(resflet.EsN0dB, resflet.NErrMissed./resflet.Nsim), hold off
title('Undetectet Packet Error Rates PER')
xlim([6,12]), grid on, grid minor
legend('PER', 'PCS-16','SUM-16','CRC-16','FS-16')


figure()
subplot(2,2,1)
semilogy(respcs.EsN0dB, respcs.NErrActual./respcs.Nsim), hold on
semilogy(respcs.EsN0dB, respcs.NErrCS./respcs.Nsim)
semilogy(respcs.EsN0dB, respcs.NErrMissed./respcs.Nsim)
semilogy(respcs.EsN0dB, respcs.NErrFalse./respcs.Nsim), hold off
title('PCS-16')
xlim([EsN0start,EsN0stop]), grid on, grid minor
legend('Actuall Err','CS det Err', 'Missed Err','False Err')

subplot(2,2,2)
semilogy(ressum.EsN0dB, ressum.NErrActual./ressum.Nsim), hold on
semilogy(ressum.EsN0dB, ressum.NErrCS./ressum.Nsim)
semilogy(ressum.EsN0dB, ressum.NErrMissed./ressum.Nsim)
semilogy(ressum.EsN0dB, ressum.NErrFalse./ressum.Nsim), hold off
title('SUM-16')
xlim([EsN0start,EsN0stop]), grid on, grid minor
legend('Actuall Err','CS det Err', 'Missed Err','False Err')

subplot(2,2,3)
semilogy(rescrc.EsN0dB, rescrc.NErrActual./rescrc.Nsim), hold on
semilogy(rescrc.EsN0dB, rescrc.NErrCS./rescrc.Nsim)
semilogy(rescrc.EsN0dB, rescrc.NErrMissed./rescrc.Nsim)
semilogy(rescrc.EsN0dB, rescrc.NErrFalse./rescrc.Nsim), hold off
title('CRC-16')
xlim([EsN0start,EsN0stop]), grid on, grid minor
legend('Actuall Err','CS det Err', 'Missed Err','False Err')

subplot(2,2,4)
semilogy(resflet.EsN0dB, resflet.NErrActual./resflet.Nsim), hold on
semilogy(resflet.EsN0dB, resflet.NErrCS./resflet.Nsim)
semilogy(resflet.EsN0dB, resflet.NErrMissed./resflet.Nsim)
semilogy(resflet.EsN0dB, resflet.NErrFalse./resflet.Nsim), hold off
title('FS-16')
xlim([EsN0start,EsN0stop]), grid on, grid minor
legend('Actuall Err','CS det Err', 'Missed Err','False Err')