close all, clear all
tic

Nsim = 100;
EsN0dB = 0;

WordLen = 16;

EsN0start = 0;
EsN0stop  = 10;

EsN0dB_save = -200:200;

Nword = 0;

for j = EsN0start+201 : 1 : EsN0stop+201
    
EsN0dB = EsN0dB_save(j);

% Derivatives
Es = 0.5;
EsN0 = 10^(EsN0dB/10);
N0 =  Es/(EsN0);
    
NErrActual = 0;
NErrCS = 0;
NErrMissed = 0;
NErrFalse = 0;

Nsim_save       = zeros(1,numel(EsN0dB_save));
NErrActual_save = zeros(1,numel(EsN0dB_save));
NErrCS_save     = zeros(1,numel(EsN0dB_save));
NErrMissed_save = zeros(1,numel(EsN0dB_save));
NErrFalse_save  = zeros(1,numel(EsN0dB_save));

if exist('results.mat', 'file') == 2
    load('results.mat','EsN0dB_save','Nsim_save','NErrActual_save','NErrCS_save','NErrMissed_save','NErrFalse_save');
end


for i = 1:Nsim

    % Generate random word
    x = randi([0,1], WordLen, 8);
    % Generate checksums
    xCS1    = sum(bin2dec(num2str(x)),1);
    
    xCS = xCS1 + xCS2 + xCS3 + xCS4;
    xCStmp = dec2bin(xCS,16);
    for n = 1:16
        xCS(n) = str2double(xCStmp(n));
    end
    % Generate noise vector 
    w = sqrt(N0/2) .* randn(WordLen,8);
    wCS = sqrt(N0/2) .* randn(1,16);
    % TX signal
    s = x + w;
    sCS = xCS + wCS;
    % Hard decoding
    y = (s > 0.5);
    yCSrx = (sCS > 0.5);
    % Calculate checksums
    yCS1 = sum(bin2dec(num2str(xor(y,cc0))),1);
    yCS2 = sum(bin2dec(num2str(xor(y,cc1))),1);
    yCS3 = sum(bin2dec(num2str(xor(y,cc2))),1);
    yCS4 = sum(bin2dec(num2str(xor(y,cc3))),1);
    yCS  = yCS1 + yCS2 + yCS3 +yCS4;
    yCStmp = dec2bin(yCS,16);
    for n = 1:16
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

Nsim_save(EsN0dB_save == EsN0dB) = Nsim_save(EsN0dB_save == EsN0dB) + Nsim;
NErrActual_save(EsN0dB_save == EsN0dB) = NErrActual_save(EsN0dB_save == EsN0dB) + NErrActual;
NErrCS_save(EsN0dB_save == EsN0dB) = NErrCS_save(EsN0dB_save == EsN0dB) + NErrCS;
NErrMissed_save(EsN0dB_save == EsN0dB) = NErrMissed_save(EsN0dB_save == EsN0dB) + NErrMissed;
NErrFalse_save(EsN0dB_save == EsN0dB) = NErrFalse_save(EsN0dB_save == EsN0dB) + NErrFalse;

Nsim_save(EsN0dB_save == EsN0dB);
NErrMissed_save(EsN0dB_save == EsN0dB);
NErrFalse_save(EsN0dB_save == EsN0dB);
NErrActual_save(EsN0dB_save == EsN0dB);
NErrCS_save(EsN0dB_save == EsN0dB);

simErr = sum((NErrActual_save - NErrCS_save) - NErrMissed_save);

save('results.mat','EsN0dB_save','Nsim_save','NErrActual_save','NErrCS_save','NErrMissed_save','NErrFalse_save')

Nword = Nword + Nsim;

end

figure()
semilogy(EsN0dB_save, NErrActual_save./Nsim_save), hold on
semilogy(EsN0dB_save, NErrCS_save./Nsim_save)
semilogy(EsN0dB_save, NErrMissed_save./Nsim_save)
semilogy(EsN0dB_save, NErrFalse_save./Nsim_save)
xlim([EsN0start,EsN0stop]), grid on, grid minor
legend('Actuall Err','CS det Err', 'Missed Err','False Err')

T = toc
tavgPerWord = T/Nword
