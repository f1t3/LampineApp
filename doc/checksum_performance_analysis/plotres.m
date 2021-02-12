function f = plotres()
close all;

EsN0start = 5;
EsN0stop  = 15;

resadvflet = loadres('resultsadvflet16.mat');
resflet = loadres('resultsflet16.mat');
ressum  = loadres('resultssum16.mat');
rescrc  = loadres('resultscrc16.mat');
respcs  = loadres('resultspcs16.mat');

esnodb = -200:1:200;

nerractual = respcs.nerractual + ressum.nerractual + rescrc.nerractual + resflet.nerractual + resadvflet.nerractual;
nsim       = respcs.nsim + ressum.nsim + rescrc.nsim + resflet.nsim + resadvflet.nsim;

figure()
% Plot undetected error rates
semilogy(esnodb, nerractual       ./nsim,               '-k' , 'LineWidth',1.25 ), hold on
semilogy(esnodb, respcs.nerrmiss  ./respcs.nsim,        '-ks', 'LineWidth',1 )
semilogy(esnodb, ressum.nerrmiss  ./ressum.nsim,        '-ko', 'LineWidth',1 )
semilogy(esnodb, rescrc.nerrmiss  ./rescrc.nsim,        '-k*', 'LineWidth',1 )
semilogy(esnodb, resflet.nerrmiss ./resflet.nsim,       '-k^', 'LineWidth',1 )
semilogy(esnodb, resadvflet.nerrmiss ./resadvflet.nsim, '-kv', 'LineWidth',1 )

% Plot false error rates
% semilogy(esnodb, respcs.nerrfalse  ./respcs.nsim,  ':ks', 'LineWidth',1.5 )
% semilogy(esnodb, ressum.nerrfalse  ./ressum.nsim,  ':ko', 'LineWidth',1.5 )
% semilogy(esnodb, rescrc.nerrfalse  ./rescrc.nsim,  ':k^', 'LineWidth',1.5 )
% semilogy(esnodb, resflet.nerrfalse ./resflet.nsim, ':k*', 'LineWidth',1.5 )

title('Undetected PER')
xlim([5,12]), grid on, grid minor
legend('PER', 'PCS-16','SUM-16','CRC-16','FS-16')


figure()
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