close all, clear all
tic

Nsim = 1000;
EsN0dB = 0;

WordLen = 16;

EsN0start = 0;
EsN0stop  = 10;

EsN0dB_save_cs = -200:200;
EsN0dB_save_crc = -200:200;

Nword = 0;

for j = EsN0start+201 : 1 : EsN0stop+201
    
EsN0dB = EsN0dB_save_cs(j);

% Derivatives
Es = 0.5;
EsN0 = 10^(EsN0dB/10);
N0 =  Es/(EsN0);
    
NErrActual = 0;
NErrCS = 0;
NErrMissed = 0;
NErrFalse = 0;

Nsim_save_cs       = zeros(1,numel(EsN0dB_save_cs));
NErrActual_save_cs = zeros(1,numel(EsN0dB_save_cs));
NErrCS_save_cs     = zeros(1,numel(EsN0dB_save_cs));
NErrMissed_save_cs = zeros(1,numel(EsN0dB_save_cs));
NErrFalse_save_cs  = zeros(1,numel(EsN0dB_save_cs));
Nsim_save_crc       = zeros(1,numel(EsN0dB_save_crc));
NErrActual_save_crc = zeros(1,numel(EsN0dB_save_crc));
NErrCS_save_crc     = zeros(1,numel(EsN0dB_save_crc));
NErrMissed_save_crc = zeros(1,numel(EsN0dB_save_crc));
NErrFalse_save_crc  = zeros(1,numel(EsN0dB_save_crc));

if exist('results.mat', 'file') == 2
    load('results.mat','EsN0dB_save_cs','Nsim_save_cs','NErrActual_save_cs','NErrCS_save_cs','NErrMissed_save_cs','NErrFalse_save_cs','EsN0dB_save_crc','Nsim_save_crc','NErrActual_save_crc','NErrCS_save_crc','NErrMissed_save_crc','NErrFalse_save_crc')
end


for i = 1:Nsim
    % Generate random word
    x = randi([0,1], WordLen, 8);
    % Generate checksums
    xCS = sum(bin2dec(num2str(x)),1);
    xCS = xCS - floor(xCS/100)*100;
    xCStmp = dec2bin(xCS,8);  
    for n = 1:8
        xCS(n) = str2double(xCStmp(n));
    end
    % Generate noise vector 
    w = sqrt(N0/2) .* randn(WordLen,8);
    wCS = sqrt(N0/2) .* randn(1,8);
    
    % TX signal
    s = x + w;
    sCS = xCS + wCS;
    
    % Hard decoding
    y = (s > 0.5);
    yCSrx = (sCS > 0.5);
    % Calculate checksums
    yCS    = sum(bin2dec(num2str(y)),1);
    yCS = yCS - floor(yCS/100)*100;
    yCStmp = dec2bin(yCS,8);
    for n = 1:8
        yCS(n) = str2double(yCStmp(n));
    end
    % Check if actual word error occured
    e = sum(sum((y ~= x),2),1);
    e = e > 0;
    NErrActual = NErrActual + e;
    % Compare CS, check if word error would be detected
    eCS = sum(yCSrx ~= yCS);
    eCS = eCS > 0;
    NErrCS = NErrCS + eCS;
    % Check if error would be missed
    NErrMissed = NErrMissed + (eCS < e);
    % Check if false error
    NErrFalse = NErrFalse   + (eCS > e);
end

Nsim_save_cs(EsN0dB_save_cs == EsN0dB) = Nsim_save_cs(EsN0dB_save_cs == EsN0dB) + Nsim;
NErrActual_save_cs(EsN0dB_save_cs == EsN0dB) = NErrActual_save_cs(EsN0dB_save_cs == EsN0dB) + NErrActual;
NErrCS_save_cs(EsN0dB_save_cs == EsN0dB) = NErrCS_save_cs(EsN0dB_save_cs == EsN0dB) + NErrCS;
NErrMissed_save_cs(EsN0dB_save_cs == EsN0dB) = NErrMissed_save_cs(EsN0dB_save_cs == EsN0dB) + NErrMissed;
NErrFalse_save_cs(EsN0dB_save_cs == EsN0dB) = NErrFalse_save_cs(EsN0dB_save_cs == EsN0dB) + NErrFalse;

Nsim_save_cs(EsN0dB_save_cs == EsN0dB);
NErrMissed_save_cs(EsN0dB_save_cs == EsN0dB);
NErrFalse_save_cs(EsN0dB_save_cs == EsN0dB);
NErrActual_save_cs(EsN0dB_save_cs == EsN0dB);
NErrCS_save_cs(EsN0dB_save_cs == EsN0dB);

simErr = sum((NErrActual_save_cs - NErrCS_save_cs) - NErrMissed_save_cs);

save('results.mat','EsN0dB_save_cs','Nsim_save_cs','NErrActual_save_cs','NErrCS_save_cs','NErrMissed_save_cs','NErrFalse_save_cs','EsN0dB_save_crc','Nsim_save_crc','NErrActual_save_crc','NErrCS_save_crc','NErrMissed_save_crc','NErrFalse_save_crc')

Nword = Nword + Nsim;

end

figure()
subplot(2,1,1)
semilogy(EsN0dB_save_cs, NErrActual_save_cs./Nsim_save_cs), hold on
semilogy(EsN0dB_save_cs, NErrCS_save_cs./Nsim_save_cs)
semilogy(EsN0dB_save_cs, NErrMissed_save_cs./Nsim_save_cs)
semilogy(EsN0dB_save_cs, NErrFalse_save_cs./Nsim_save_cs), hold off
title('Simple Checksum 128-8')
xlim([EsN0start,EsN0stop]), grid on, grid minor
legend('Actuall Err','CS det Err', 'Missed Err','False Err')

subplot(2,1,2)
semilogy(EsN0dB_save_crc, NErrActual_save_crc./Nsim_save_crc), hold on
semilogy(EsN0dB_save_crc, NErrCS_save_crc./Nsim_save_crc)
semilogy(EsN0dB_save_crc, NErrMissed_save_crc./Nsim_save_crc)
semilogy(EsN0dB_save_crc, NErrFalse_save_crc./Nsim_save_crc), hold off
title('CRC 128-8')
xlim([EsN0start,EsN0stop]), grid on, grid minor
legend('Actuall Err','CS det Err', 'Missed Err','False Err')

T = toc
tavgPerWord = T/Nword
