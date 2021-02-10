function f = plotres()
close all;

EsN0start = 5;
EsN0stop  = 15;

resflet = loadres('resultsflet16.mat');
ressum  = loadres('resultssum16.mat');
rescrc  = loadres('resultscrc16.mat');
respcs  = loadres('resultspcs16.mat');

esnodb = -200:1:200;

nerractual = respcs.nerractual + ressum.nerractual + rescrc.nerractual + resflet.nerractual;
nsim       = respcs.nsim + ressum.nsim + rescrc.nsim + resflet.nsim;

figure()
semilogy(esnodb, nerractual./nsim), hold on
% semilogy(respcs.esnodb, respcs.nerrmiss./respcs.nsim)
% semilogy(ressum.esnodb, ressum.nerrmiss./ressum.nsim)
% semilogy(rescrc.esnodb, rescrc.nerrmiss./rescrc.nsim)
semilogy(esnodb, resflet.nerrmiss./resflet.nsim), hold off
title('Undetected PER')
xlim([6,12]), grid on, grid minor
legend('PER', 'PCS-16','SUM-16','CRC-16','FS-16')


% figure()
% subplot(2,2,1)
% semilogy(respcs.esnodb, respcs.nerractual./respcs.nsim), hold on
% semilogy(respcs.esnodb, respcs.NErrCS./respcs.nsim)
% semilogy(respcs.esnodb, respcs.nerrmiss./respcs.nsim)
% semilogy(respcs.esnodb, respcs.NErrFalse./respcs.nsim), hold off
% title('PCS-16')
% xlim([EsN0start,EsN0stop]), grid on, grid minor
% legend('Actuall Err','CS det Err', 'Missed Err','False Err')
% 
% subplot(2,2,2)
% semilogy(ressum.esnodb, ressum.nerractual./ressum.nsim), hold on
% semilogy(ressum.esnodb, ressum.NErrCS./ressum.nsim)
% semilogy(ressum.esnodb, ressum.nerrmiss./ressum.nsim)
% semilogy(ressum.esnodb, ressum.NErrFalse./ressum.nsim), hold off
% title('SUM-16')
% xlim([EsN0start,EsN0stop]), grid on, grid minor
% legend('Actuall Err','CS det Err', 'Missed Err','False Err')
% 
% subplot(2,2,3)
% semilogy(rescrc.esnodb, rescrc.nerractual./rescrc.nsim), hold on
% semilogy(rescrc.esnodb, rescrc.NErrCS./rescrc.nsim)
% semilogy(rescrc.esnodb, rescrc.nerrmiss./rescrc.nsim)
% semilogy(rescrc.esnodb, rescrc.NErrFalse./rescrc.nsim), hold off
% title('CRC-16')
% xlim([EsN0start,EsN0stop]), grid on, grid minor
% legend('Actuall Err','CS det Err', 'Missed Err','False Err')

subplot(2,2,4)
semilogy(resflet.esnodb, resflet.nerractual./resflet.nsim), hold on
semilogy(resflet.esnodb, resflet.nerrdet./resflet.nsim)
semilogy(resflet.esnodb, resflet.nerrmiss./resflet.nsim)
semilogy(resflet.esnodb, resflet.nerrfalse./resflet.nsim), hold off
title('FS-16')
xlim([EsN0start,EsN0stop]), grid on, grid minor
legend('Actuall Err','CS det Err', 'Missed Err','False Err')

end