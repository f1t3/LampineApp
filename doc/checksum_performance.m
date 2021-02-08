close all, clear all
tic

Nsim =1000000;
EsN0start = 11;
EsN0stop  =  11;

EsN0dB_save = -200:200;

Nword = 0;
for i = EsN0start+201 : 1 : EsN0stop+201

EsN0dB = EsN0dB_save(i);
WordLen = 14;

% Derivatives
Es = 0.5;
EsN0 = 10^(EsN0dB/10);
N0 =  Es/(EsN0);
    
NErrActual = 0;
NErrCS = 0;
NErrMissed = 0;

Nsim_save = zeros(1,numel(EsN0dB_save));
NErrActual_save = zeros(1,numel(EsN0dB_save));
NErrCS_save = zeros(1,numel(EsN0dB_save));
NErrMissed_save = zeros(1,numel(EsN0dB_save));

if exist('results.mat', 'file') == 2
    load('results.mat','EsN0dB_save','Nsim_save','NErrActual_save','NErrCS_save','NErrMissed_save');
end


for i = 1:Nsim
    % Generate random word
    x = randi([0,1], WordLen, 8);
    % Generate checksums
    xCS = sum(bin2dec(num2str(x)),1);
    % Generate noise vector 
    w = sqrt(N0/2) .* randn(WordLen,8);
    % TX signal
    s = x + w;
    % Hard decoding
    y = (s > 0.5);
    % Calculate checksums
    yCS = sum(bin2dec(num2str(y)),1);
    % Check if actual word error occured
    e = sum(sum((y ~= x),2),1);
    e = e ~= 0;
    NErrActual = NErrActual + e;
    % Compare CS, check if word error would be detected
    eCS = xCS ~= yCS;
    NErrCS = NErrCS + eCS;
    % Check if error would be missed
    NErrMissed = NErrMissed + (eCS ~= e);
end


Nsim_save(EsN0dB_save == EsN0dB) = Nsim_save(EsN0dB_save == EsN0dB) + Nsim;
NErrActual_save(EsN0dB_save == EsN0dB) = NErrActual_save(EsN0dB_save == EsN0dB) + NErrActual;
NErrCS_save(EsN0dB_save == EsN0dB) = NErrCS_save(EsN0dB_save == EsN0dB) + NErrCS;
NErrMissed_save(EsN0dB_save == EsN0dB) = NErrMissed_save(EsN0dB_save == EsN0dB) + NErrMissed;

Nsim_save(EsN0dB_save == EsN0dB);
NErrMissed_save(EsN0dB_save == EsN0dB);
NErrActual_save(EsN0dB_save == EsN0dB);
NErrCS_save(EsN0dB_save == EsN0dB);
simErr = sum((NErrActual_save - NErrCS_save) - NErrMissed_save);

save('results.mat','EsN0dB_save','Nsim_save','NErrActual_save','NErrCS_save','NErrMissed_save')

Nword = Nword + Nsim;

end

figure()
semilogy(EsN0dB_save, NErrActual_save./Nsim_save), hold on
semilogy(EsN0dB_save, NErrCS_save./Nsim_save),
semilogy(EsN0dB_save, NErrMissed_save./Nsim_save),
xlim([0,15]), grid on, grid minor
legend('Actuall Err','CS det Err', 'Missed Err')

T = toc
tavgPerWord = T/Nword
